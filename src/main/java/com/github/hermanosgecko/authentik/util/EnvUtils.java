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

import java.text.MessageFormat;

public class EnvUtils{

	public static String getString(String name) throws MissingValueException {
		String value = System.getenv(name);
		if(value == null || value == "") {
			throw new MissingValueException(name);
		}
		return value;
	}
	
	public static Integer getInt(String name) throws MissingValueException,InvalidIntegerException {
		try {
			String rawVal = getString(name);
			return Integer.parseInt(rawVal);
		}catch(NumberFormatException ex) {
			throw new InvalidIntegerException(name);
		}
	}

	
	public static Boolean getBool(String name) throws MissingValueException  {
		String rawVal = getString(name);
		return Boolean.valueOf(rawVal);
	}
	
	public static class MissingValueException extends Exception{

		private static final long serialVersionUID = 1L;
		
		public MissingValueException(String name) {
			super(MessageFormat.format("Environment Variable {0} is required", name));
		}
	}
	
	public static class InvalidIntegerException extends Exception{

		private static final long serialVersionUID = 1L;
		
		public InvalidIntegerException(String name) {
			super(MessageFormat.format("Environment Variable {0} must be an integer", name));
		}
	}
	
}
