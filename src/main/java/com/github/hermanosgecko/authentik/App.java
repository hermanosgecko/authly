/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.hermanosgecko.authentik;

import static spark.Spark.get;
import static spark.Spark.notFound;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.text.MessageFormat;
import java.time.Instant;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hermanosgecko.authentik.token.TokenException;
import com.github.hermanosgecko.authentik.token.TokenUtils;
import com.github.hermanosgecko.authentik.user.UserService;

public class App {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	private static final String COOKIE_NAME = "authentik.token";

	private static final UserService userService = new UserService();

	private static String cookieDomain;
	private static boolean insecureCookie = false;
	private static String authHost;
	private static int cookieLifetime = 86400;
	private static String pwdFile = "/htpasswd";
	private static String secret;

	public static void main(String[] args) {

		LOGGER.info("Starting");

		// create command line options
		final Options options = new Options()
				.addOption(Option.builder()
						.longOpt("cookie-domain")
						.desc("cookie domain")
						.hasArg()
						.required()
						.type(String.class)
						.build())
				.addOption(Option.builder("i")
						.longOpt("insecure-cookie")
						.desc("insecure cookie")
						.hasArg()
						//.required()
						.type(Boolean.class)
						.build())
				.addOption(Option.builder()
						.longOpt("auth-host")
						.desc("authentication hostname")
						.hasArg()
						.required()
						.type(String.class)
						.build())
				.addOption(Option.builder()
						.longOpt("lifetime")
						.desc("validity period")
						.hasArg()
						//.required()
						.type(Integer.class)
						.build())
				.addOption(Option.builder()
						.longOpt("secret")
						.desc("token secret")
						.hasArg()
						.required()
						.type(String.class)
						.build())
				.addOption(Option.builder()
						.longOpt("file")
						.desc("htpasswd file")
						.hasArg()
						//.required()
						.type(String.class)
						.build());

		// parse the command line arguments
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd;
		try {
			cmd = parser.parse( options, args);
		} catch(MissingOptionException e) {
			LOGGER.error(e.getLocalizedMessage());
			return;			
		}
		catch (ParseException e) {
			LOGGER.error("Unexpected error", e);
			return;
		}

		cookieDomain = cmd.getOptionValue("cookie-domain");
		LOGGER.info(MessageFormat.format("cookie domain set to {0}", cookieDomain ));

		if(cmd.hasOption("insecure-cookie")) {
			insecureCookie = Boolean.valueOf(cmd.getOptionValue("insecure-cookie"));
		}
		LOGGER.info(MessageFormat.format("insecure cookie set to {0}", insecureCookie ));

		
		authHost = cmd.getOptionValue("auth-host");
		LOGGER.info(MessageFormat.format("auth host set to {0}", authHost ));

		if(cmd.hasOption("lifetime")) {
			try {
				cookieLifetime = Integer.valueOf(cmd.getOptionValue("lifetime"));
			} catch (NumberFormatException e) {
				LOGGER.error("Unexpected error", e);
				return;
			}
		}
		LOGGER.info(MessageFormat.format("lifetime set to {0,number,#}", cookieLifetime ));

		if(cmd.hasOption("file")) {
			pwdFile = cmd.getOptionValue("file");
		}
		LOGGER.info(MessageFormat.format("htpasswd file set to {0}", pwdFile ));

		secret = cmd.getOptionValue("secret");
		LOGGER.info(MessageFormat.format("token secret set to {0}", secret ));

		userService.setFileName(pwdFile);

		staticFiles.location("/static");

		notFound((request, response) -> {
			response.redirect("/auth", 301);
			return "";
		});

		get("/auth", (request, response) -> {

			final String fwd_host = request.headers("X-Forwarded-Host");
			final String fwd_uri = request.headers("X-Forwarded-Uri");
			final String fwd_method = request.headers("X-Forwarded-Method");

			if(fwd_host == null || fwd_uri == null || fwd_method == null) {
				response.status(503);
				return response;
			}

			if(fwd_host.equals(authHost)) {
				response.status(200);
				return response;
			}

			final String token = request.cookie(COOKIE_NAME);
			try {

				String user = TokenUtils.validate(secret, cookieDomain, token);
				response.header("X-Forwarded-User", user);
				response.status(200);
				return response;

			}catch(TokenException ex) {		      	
				response.removeCookie(COOKIE_NAME);
				response.redirect(MessageFormat.format("http://{0}/?redirect_uri={1}", authHost, Utils.buildRedirectUri(request)),307);
				return response;		
			}

		});

		post("/login", (request, response) -> {

			final String redirect_uri = request.queryParams("redirect_uri");
			final String username = request.queryParams("login");
			final String password = request.queryParams("password");

			if(redirect_uri == null ||  username == null || password == null) {
				response.status(503);
				return response;
			}

			if(userService.authenticate(username, password)) {
				response.removeCookie(COOKIE_NAME);

				String token = TokenUtils.create(secret, cookieDomain, username, Instant.now().plusSeconds(cookieLifetime).toEpochMilli());          	
				response.cookie(cookieDomain, "/", COOKIE_NAME, token, cookieLifetime, !insecureCookie, true);
				response.header("X-Forwarded-User", username);

				response.redirect(redirect_uri);
				return response;
			}

			// if not authenticated return error
			response.status(401);
			return response;
		});

	}

}
