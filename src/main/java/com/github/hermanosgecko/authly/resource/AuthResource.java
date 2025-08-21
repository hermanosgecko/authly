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
package com.github.hermanosgecko.authly.resource;

import java.io.UnsupportedEncodingException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;
import java.net.URLEncoder;
import java.text.MessageFormat;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import io.quarkus.security.identity.SecurityIdentity;

@Path("/auth")
public class AuthResource {

    private final Logger LOGGER = System.getLogger(AuthResource.class.getName());

    @Inject
    @ConfigProperty(name="authly.host")
    String authlyHost;

    @Inject
    @ConfigProperty(name="authly.user.header")
    String authlyUserHeader;

    @Inject
    SecurityIdentity identity;

    @Context
    HttpHeaders httpHeaders;

    @Context
    UriInfo uriInfo;

    @GET
    public Response get() 
    {
        if (identity.isAnonymous()) {
            LOGGER.log(Level.DEBUG, "Redirecting To Login Page");
            String fwd_proto = httpHeaders.getHeaderString("X-Forwarded-Proto");
            String fwd_host = httpHeaders.getHeaderString("X-Forwarded-Host");
            String fwd_uri = httpHeaders.getHeaderString("X-Forwarded-Uri");

            final String redirect_uri = buildRedirectUri(fwd_proto,fwd_host,fwd_uri);
			
            URI redirectToUri = UriBuilder.fromUri(authlyHost+"/login")
                                    .queryParam("redirect_uri", redirect_uri)
                                    .build();

            LOGGER.log(Level.DEBUG, "Redirecting To {0}", redirectToUri);
            return Response.status(Status.FOUND).location(redirectToUri).build();
        }

        String user = identity.getPrincipal().getName();
        return Response.status(Status.OK).header(authlyUserHeader, user).build();
    }

	public String buildRedirectUri(final String proto, final String host, final String uri) {
		try {
			return URLEncoder.encode(MessageFormat.format("{0}://{1}{2}", proto, host, uri), "UTF-8");
		} catch(UnsupportedEncodingException | IllegalArgumentException ex) {
			LOGGER.log(Level.ERROR, "Unable to URL encode the redirect uri", ex);
            // redirect to the index
            return "/";
		}
	}

}
