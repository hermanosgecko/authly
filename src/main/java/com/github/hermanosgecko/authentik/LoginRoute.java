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

import java.time.Instant;

import com.github.hermanosgecko.authentik.util.HttpHeaders;
import com.github.hermanosgecko.authentik.util.TokenUtils;

import spark.Request;
import spark.Response;
import spark.Route;

public class LoginRoute implements Route {

	private final UserService userService;
	private final String secret;
	private final String cookieName;
	private final String cookieDomain;
	private final boolean insecureCookie;
	private final int cookieLifetime;
	
	public LoginRoute(UserService userService, String secret, String cookieName, String cookieDomain, boolean insecureCookie, int cookieLifetime) {
		this.userService = userService;
		this.secret = secret;
		this.cookieName = cookieName;
		this.cookieDomain = cookieDomain;
		this.insecureCookie = insecureCookie;
		this.cookieLifetime = cookieLifetime;
	}
	
	/**
	 * @return the userService
	 */
	public UserService getUserService() {
		return userService;
	}

	/**
	 * @return the secret
	 */
	public String getSecret() {
		return secret;
	}

	/**
	 * @return the cookieName
	 */
	public String getCookieName() {
		return cookieName;
	}

	/**
	 * @return the cookieDomain
	 */
	public String getCookieDomain() {
		return cookieDomain;
	}

	/**
	 * @return the insecureCookie
	 */
	public boolean isInsecureCookie() {
		return insecureCookie;
	}

	/**
	 * @return the cookieLifetime
	 */
	public int getCookieLifetime() {
		return cookieLifetime;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		final String redirect_uri = request.queryParams("redirect_uri");
		final String username = request.queryParams("login");
		final String password = request.queryParams("password");

		if(redirect_uri == null ||  username == null || password == null) {
			response.status(503);
			return response;
		}

		if(userService.authenticate(username, password)) {
			response.removeCookie(cookieName);

			String token = TokenUtils.create(secret, cookieDomain, username, Instant.now().plusSeconds(cookieLifetime).toEpochMilli());          	
			response.cookie(cookieDomain, "/", cookieName, token, cookieLifetime, !insecureCookie, true);
			response.header(HttpHeaders.X_FORWARDED_USER, username);

			response.redirect(redirect_uri);
			return response;
		}

		// if not authenticated return error
		response.status(401);
		return response;
	}

}
