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

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class TokenUtils {

	public static class TokenException extends Exception {

		private static final long serialVersionUID = 1L;

		public TokenException(String message) {
			super(message);
		}

		public TokenException(String message, Throwable ex) {
			super(message, ex);
		}
	}

	public static String createHMAC(String secret, String cookieDomain, String username, long expiry) throws TokenException {
		try {
			Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
			SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
			sha256_HMAC.init(secret_key);

			ByteBuffer buff = ByteBuffer.allocate(cookieDomain.getBytes().length + username.getBytes().length + 8);
			buff.put(cookieDomain.getBytes());
			buff.put(username.getBytes());
			buff.putLong(expiry);
			return Base64.encodeBase64String(sha256_HMAC.doFinal(buff.array()));
		} catch (NoSuchAlgorithmException | InvalidKeyException ex) {
			throw new TokenException("Unable to generate HMAC", ex);
		}
	}

	public static String create(String secret, String cookieDomain, String username, long expiry)
			throws TokenException {
		String mac = createHMAC(secret, cookieDomain, username, expiry);
		return MessageFormat.format("{0}|{1}|{2,number,#}", mac, username, expiry);
	}

	public static String validate(String secret, String cookieDomain, String token) throws TokenException {
		if (token == null)
			throw new TokenException("No token");

		String[] parts = token.split("\\|");
		if (parts.length != 3) {
			throw new TokenException("Invalid cookie format");
		}

		String returnedMac = parts[0];
		String username = parts[1];
		long expiry;
		try {
			expiry = Long.parseLong(parts[2]);
		} catch (NumberFormatException ex) {
			throw new TokenException("Unable to parse cookie expiry");
		}

		String expectedMac = createHMAC(secret, cookieDomain, username, expiry);
		if (!returnedMac.equals(expectedMac)) {
			throw new TokenException("Invalid cookie mac");

		}

		if (Instant.ofEpochMilli(expiry).isBefore(Instant.now())) {
			throw new TokenException("Cookie expired");
		}

		// if everything passes it must be valid
		return username;
	}
}
