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

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.UnauthorizedException;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class IndexResource {

    private final Logger logger = System.getLogger(IndexResource.class.getName());

    @Inject
    Template authenticated;

    @Inject
    SecurityIdentity identity;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance doGet(){
        logger.log(Level.DEBUG, "Entering");

        // Check if a user is logged in
        if (identity.isAnonymous()) {
            String message = "Not authenticated";
            logger.log(Level.DEBUG, message);
            throw new UnauthorizedException(message);
        }
        
        String name = identity.getPrincipal().getName();
        logger.log(Level.DEBUG, "User \"{0}\" is authenticated", name);
        return authenticated.data("name", name);
    }

}
