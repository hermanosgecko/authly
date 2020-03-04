package com.github.hermanosgecko.authentik;

import java.time.Instant;

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
	
	public LoginRoute(String fileName, String secret, String cookieName, String cookieDomain, boolean insecureCookie, int cookieLifetime) {
		this.userService = new UserService(fileName);
		this.secret = secret;
		this.cookieName = cookieName;
		this.cookieDomain = cookieDomain;
		this.insecureCookie = insecureCookie;
		this.cookieLifetime = cookieLifetime;
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
