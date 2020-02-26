package com.github.hermanosgecko.authentik.token;

import java.time.Instant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.hermanosgecko.authentik.token.TokenException;
import com.github.hermanosgecko.authentik.token.TokenUtils;

public class TokenUtils_Test {

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
