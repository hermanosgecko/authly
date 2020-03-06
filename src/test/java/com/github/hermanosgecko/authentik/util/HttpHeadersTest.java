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
package com.github.hermanosgecko.authentik.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpHeadersTest {

	@Test
	public void forwardedHostTest() {
		Assertions.assertEquals("X-Forwarded-Host", HttpHeaders.X_FORWARDED_HOST);
	}
	
	@Test
	public void forwardedMethodTest() {
		Assertions.assertEquals("X-Forwarded-Method", HttpHeaders.X_FORWARDED_METHOD);
	}
	
	@Test
	public void forwardedPortTest() {
		Assertions.assertEquals("X-Forwarded-Port", HttpHeaders.X_FORWARDED_PORT);
	}
	
	@Test
	public void forwardedUriTest() {
		Assertions.assertEquals("X-Forwarded-Uri", HttpHeaders.X_FORWARDED_URI);
	}
	
	@Test
	public void forwardedProtoTest() {
		Assertions.assertEquals("X-Forwarded-Proto", HttpHeaders.X_FORWARDED_PROTO);
	}
	
	@Test
	public void forwardedUserTest() {
		Assertions.assertEquals("X-Forwarded-User", HttpHeaders.X_FORWARDED_USER);
	}
}
