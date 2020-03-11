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

import static spark.Spark.halt;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import com.github.hermanosgecko.authentik.util.HttpHeaders;
import com.github.hermanosgecko.authentik.util.TokenUtils;
import com.github.hermanosgecko.authentik.util.TokenUtils.TokenException;

import spark.Request;
import spark.Response;
import spark.Route;

public class AuthRoute implements Route {

	private final String authHost;
	private final String secret;
	private final String cookieName;
	private final String cookieDomain;
	
	public AuthRoute(String authHost, String secret, String cookieName, String cookieDomain) {
		this.authHost = authHost;
		this.secret = secret;
		this.cookieName = cookieName;
		this.cookieDomain = cookieDomain;
	}
	
	/**
	 * @return the authHost
	 */
	public String getAuthHost() {
		return authHost;
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

	@Override
	public Object handle(Request request, Response response) throws Exception {
		final String fwd_host = request.headers(HttpHeaders.X_FORWARDED_HOST);
		final String fwd_uri = request.headers(HttpHeaders.X_FORWARDED_URI);
		final String fwd_method = request.headers(HttpHeaders.X_FORWARDED_METHOD);
		
		if(fwd_host == null || fwd_uri == null || fwd_method == null) {
			halt(400, "Missing required information");
		}

		if(fwd_host.equals(authHost)) {
			halt(200);
		}
		
		final String token = request.cookie(cookieName);
		try {

			String user = TokenUtils.validate(secret, cookieDomain, token);
			response.header(HttpHeaders.X_FORWARDED_USER, user);
			response.status(200);

		}catch(TokenException ex) {		      	
			response.removeCookie(cookieName);
			response.redirect(MessageFormat.format("http://{0}/?redirect_uri={1}", authHost, buildRedirectUri(request)));
		}
		return response;
	}
	
	public String buildRedirectUri(Request request) {
		final String proto = request.headers(HttpHeaders.X_FORWARDED_PROTO);
		final String host = request.headers(HttpHeaders.X_FORWARDED_HOST);
		final String uri = request.headers(HttpHeaders.X_FORWARDED_URI);

		String redirect_uri = "";
		try {
			redirect_uri = URLEncoder.encode(MessageFormat.format("{0}://{1}{2}", proto, host, uri), "UTF-8");
		} catch(UnsupportedEncodingException ex) {
			//This should never happen
		}
		return redirect_uri;
	}

}