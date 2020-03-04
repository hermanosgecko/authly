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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	private static String cookieName = "authentik.token";
	private static String cookieDomain;
	private static boolean insecureCookie = false;
	private static int cookieLifetime = 86400;
	private static String authHost;
	private static String pwdFile = "/htpasswd";
	private static String secret;

	public static void main(String[] args) {

		LOGGER.info("Starting");

		// create command line options
		Options options = getOptions();

		// parse the command line arguments
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch(MissingOptionException e) {
			LOGGER.info("Missing Option",e);
			new HelpFormatter().printHelp("Authentik [options]", options);
			System.exit(0);		
		} catch (ParseException e) {
			LOGGER.error("Unexpected error", e);
			System.exit(-1);
		}
		
		processHelpArg(cmd, options);
		processCookieDomainArg(cmd);
		processInsecureCookieArg(cmd);
		processAuthHostArg(cmd);
		processLifetimeArg(cmd);
		processFileArg(cmd);
		processSecretArg(cmd);
		
		staticFiles.location("/static");

		final NotFoundRoute notFoundRoute = new NotFoundRoute();
		final AuthRoute authRoute = new AuthRoute(authHost, secret, cookieName, cookieDomain);
		final LoginRoute loginRoute = new LoginRoute(pwdFile, secret, cookieName, cookieDomain, insecureCookie, cookieLifetime);
		
		notFound(notFoundRoute);
		
		get("/auth", authRoute);

		post("/login", loginRoute);
	}
	
	public static Options getOptions() {
		return new Options()
	    .addOption(getHelpOption())
		.addOption(getCookieDomainOption())
		.addOption(getInsecureCookieOption())
		.addOption(getAuthHostOption())
		.addOption(getLifetimeOption())
		.addOption(getSecretOption())
		.addOption(getFileOption());
	}
	
	public static Option getHelpOption() {
		return Option.builder("h")
		.longOpt("help")
		.desc("Show this help message")
		.build();
	}
	
	public static Option getCookieDomainOption() {
		return Option.builder()
		.longOpt("cookie-domain")
		.desc("Domain to set auth cookie on (required)")
		.hasArg()
		.required()
		.type(String.class)
		.build();
	}
	
	public static Option getInsecureCookieOption() {
		return Option.builder("i")
		.longOpt("insecure-cookie")
		.desc("Use insecure cookies")
		.hasArg()
		.type(Boolean.class)
		.build();
	}
	
	public static Option getAuthHostOption() {
		return Option.builder()
		.longOpt("auth-host")
		.desc("External hostname to access login page (required)")
		.hasArg()
		.required()
		.type(String.class)
		.build();
	}
	
	public static Option getLifetimeOption() {
		return Option.builder()
		.longOpt("lifetime")
		.desc("Lifetime in seconds (default: 86400)")
		.hasArg()
		.type(Integer.class)
		.build();
	}
	
	public static Option getSecretOption() {
		return Option.builder()
		.longOpt("secret")
		.desc("Secret used for signing (required)")
		.hasArg()
		.required()
		.type(String.class)
		.build();
	}
	
	public static Option getFileOption() {
		return Option.builder()
		.longOpt("file")
		.desc("Path & File name to use for htpasswd file (default: /htpasswd)")
		.hasArg()
		.type(String.class)
		.build();
	}
	
	public static void processHelpArg(CommandLine cmd, Options options) {
		if (cmd.hasOption("help")) {
			new HelpFormatter().printHelp("Authentik [options]", options);
			System.exit(0);
		}
	}
	
	public static void processCookieDomainArg(CommandLine cmd) {
		cookieDomain = cmd.getOptionValue("cookie-domain");
		LOGGER.info(MessageFormat.format("cookie domain set to {0}", cookieDomain ));
	}
	
	public static void processInsecureCookieArg(CommandLine cmd) {
		if(cmd.hasOption("insecure-cookie")) {
			insecureCookie = Boolean.valueOf(cmd.getOptionValue("insecure-cookie"));
		}
		LOGGER.info(MessageFormat.format("insecure cookie set to {0}", insecureCookie ));
	}
	
	public static void processAuthHostArg(CommandLine cmd) {
		authHost = cmd.getOptionValue("auth-host");
		LOGGER.info(MessageFormat.format("auth host set to {0}", authHost ));
	}
	
	public static void processLifetimeArg(CommandLine cmd){
		if(cmd.hasOption("lifetime")) {
			try {
				cookieLifetime = Integer.valueOf(cmd.getOptionValue("lifetime"));
			} catch (NumberFormatException e) {
				LOGGER.error("Unexpected error", e);
				System.exit(-1);
			}
		}
		LOGGER.info(MessageFormat.format("lifetime set to {0,number,#}", cookieLifetime ));
	}
	
	public static void processFileArg(CommandLine cmd){
		if(cmd.hasOption("file")) {
			pwdFile = cmd.getOptionValue("file");
		}
		LOGGER.info(MessageFormat.format("htpasswd file set to {0}", pwdFile ));
	
	}
	
	public static void processSecretArg(CommandLine cmd){
		secret = cmd.getOptionValue("secret");
		LOGGER.info(MessageFormat.format("token secret set to {0}", secret ));
	}

}
