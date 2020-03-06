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

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.hermanosgecko.authentik.util.TokenUtils;
import com.github.hermanosgecko.authentik.util.TokenUtils.TokenException;

public class TokenUtilsTest {

	@Test
	public void createHMACTest() throws Exception {
		String mac = TokenUtils.createHMAC("THIS_IS_A_SECRET", "localhost", "username", Instant.EPOCH.toEpochMilli());
		Assertions.assertEquals("JcXG5OvhcbteAEGC+BtIq8qbUun3LcmTCaH7pko3wag=", mac);
	}
	
	@Test
	public void createTokenTest() throws Exception {
		String token = TokenUtils.create("THIS_IS_A_SECRET", "localhost", "username", Instant.EPOCH.toEpochMilli());
		Assertions.assertEquals("JcXG5OvhcbteAEGC+BtIq8qbUun3LcmTCaH7pko3wag=|username|0", token);
	}
	
	@Test
	public void validateTest() throws TokenException {
		String username = TokenUtils.validate("THIS_IS_A_SECRET", "localhost", "+mWx6vTwBClJAcr0rx4ppUxVZxpvkoWU64C1ylHhqbA=|username|9223372036854775807");
		Assertions.assertEquals("username", username);
	}
}
