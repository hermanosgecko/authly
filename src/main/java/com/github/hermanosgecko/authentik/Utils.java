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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import spark.Request;

public class Utils {
		
	public static String buildRedirectUri(Request request) {
		final String proto = request.headers("X-Forwarded-Proto");
		final String host = request.headers("X-Forwarded-Host");
		final String uri = request.headers("X-Forwarded-Uri");
		String redirect_uri = "";
		try {
			redirect_uri = URLEncoder.encode(MessageFormat.format("{0}://{1}{2}", proto, host, uri), "UTF-8");
		} catch(UnsupportedEncodingException ex) {
			//This should never happen
		}
		return redirect_uri;
	}
		
}
