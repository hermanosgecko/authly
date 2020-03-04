package com.github.hermanosgecko.authentik;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

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
	
	public String getAuthHost() {
		return authHost;
	}

	public String getSecret() {
		return secret;
	}

	public String getCookieName() {
		return cookieName;
	}

	public String getCookieDomain() {
		return cookieDomain;
	}

	@Override
	public Object handle(Request request, Response response) throws Exception {
		final String fwd_host = request.headers(HttpHeaders.X_FORWARDED_HOST);
		final String fwd_uri = request.headers(HttpHeaders.X_FORWARDED_URI);
		final String fwd_method = request.headers(HttpHeaders.X_FORWARDED_METHOD);

		if(fwd_host == null || fwd_uri == null || fwd_method == null) {
			response.status(503);
			return response;
		}

		if(fwd_host.equals(authHost)) {
			response.status(200);
			return response;
		}

		final String token = request.cookie(cookieName);
		try {

			String user = TokenUtils.validate(secret, cookieDomain, token);
			response.header(HttpHeaders.X_FORWARDED_USER, user);
			response.status(200);
			return response;

		}catch(TokenException ex) {		      	
			response.removeCookie(cookieName);
			response.redirect(MessageFormat.format("http://{0}/?redirect_uri={1}", authHost, buildRedirectUri(request)),307);
			return response;		
		}
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
