package com.github.hermanosgecko.authentik;

import spark.Request;
import spark.Response;
import spark.Route;

public class NotFoundRoute implements Route {

	@Override
	public Object handle(Request request, Response response) throws Exception {
		response.redirect("/auth", 301);
		return response;
	}

}
