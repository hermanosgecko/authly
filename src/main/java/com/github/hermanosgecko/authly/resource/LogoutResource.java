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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;

import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.FormAuthenticationMechanism;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;

@Path("/logout")
public class LogoutResource {

    private final Logger logger = System.getLogger(LogoutResource.class.getName());

    @Inject
    SecurityIdentity identity;

    @POST
    public Response logout() {
        logger.log(Level.INFO, "Entering logout");
        if (identity.isAnonymous()) {
            throw new UnauthorizedException("Not authenticated");
        }
        logger.log(Level.INFO, "Identity {0}", identity);
        logger.log(Level.INFO, "User {0}", identity.getPrincipal().getName());

        FormAuthenticationMechanism.logout(identity); 
        URI redirectTo = UriBuilder.fromResource(IndexResource.class).build();

        logger.log(Level.INFO, "Redirect URI {0}", redirectTo.toString());
        return Response.status(Status.FOUND).location(redirectTo).build();
    }

}
