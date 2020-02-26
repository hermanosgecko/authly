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
	
/*	
	public static String extractRedirectUri(Request request) {
		final String referer = request.headers("Referer");
		String redirectUrl = "";
		if(referer != null) {

			try {
				redirectUrl = splitQuery(new URL(referer)).get("redirect_uri").get(0);
			} 
			catch (UnsupportedEncodingException | MalformedURLException e) {
				//TODO logging
				// do nothing
			}
		}
		return redirectUrl;
	}
	
	public static Map<String, List<String>> splitQuery(URL url) throws UnsupportedEncodingException {
		  final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
		  final String[] pairs = url.getQuery().split("&");
		  for (String pair : pairs) {
		    final int idx = pair.indexOf("=");
		    final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
		    if (!query_pairs.containsKey(key)) {
		      query_pairs.put(key, new LinkedList<String>());
		    }
		    final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
		    query_pairs.get(key).add(value);
		  }
		  return query_pairs;
		}
*/	
}
