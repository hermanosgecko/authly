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

import static spark.Spark.after;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hermanosgecko.authentik.util.EnvUtils;
import com.github.hermanosgecko.authentik.util.EnvUtils.InvalidIntegerException;
import com.github.hermanosgecko.authentik.util.EnvUtils.MissingValueException;
import com.github.hermanosgecko.authentik.util.SparkUtils;

public class App {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	private static String cookieName;
	private static String cookieDomain;
	private static boolean insecureCookie;
	private static int cookieLifetime;
	private static String authHost;
	private static String pwdFile;
	private static String secret;
	
	public static void main(String[] args) {

		LOGGER.info("Starting");
		
		try {
			cookieName = EnvUtils.getString("COOKIE_NAME");
			cookieDomain = EnvUtils.getString("COOKIE_DOMAIN");
			insecureCookie = EnvUtils.getBool("INSECURE_COOKIE");
			cookieLifetime = EnvUtils.getInt("LIFETIME");
			authHost = EnvUtils.getString("AUTH_HOST");
			secret = EnvUtils.getString("SECRET");
			pwdFile = EnvUtils.getString("FILE");
		} catch (MissingValueException | InvalidIntegerException ex) {
			LOGGER.error(ex.getMessage());
			return;
		}
		
		LOGGER.info(MessageFormat.format("{0} / {1}","cookieName", cookieName ));
		LOGGER.info(MessageFormat.format("{0} / {1}","cookieDomain", cookieDomain ));
		LOGGER.info(MessageFormat.format("{0} / {1}","insecureCookie", insecureCookie ));
		LOGGER.info(MessageFormat.format("{0} / {1}","lifetime", cookieLifetime ));
		LOGGER.info(MessageFormat.format("{0} / {1}", "authHost", authHost ));
		LOGGER.info(MessageFormat.format("{0} / {1}","secret", secret ));
		LOGGER.info(MessageFormat.format("{0} / {1}","file", pwdFile ));
	
		staticFiles.location("/static");
		
		after("*", SparkUtils.addGzipHeader);

		get("/auth", new AuthRoute(authHost, secret, cookieName, cookieDomain));

		post("/login", new LoginRoute(new UserServiceImpl(pwdFile), secret, cookieName, cookieDomain, insecureCookie, cookieLifetime));

	}

}