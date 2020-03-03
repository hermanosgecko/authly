package com.github.hermanosgecko.authentik;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class OptionBuilder {

	public Options getOptions() {
		return new Options()
	    .addOption(getHelp())
		.addOption(getCookieDomain())
		.addOption(getInsecureCookie())
		.addOption(getAuthHost())
		.addOption(getLifetime())
		.addOption(getSecret())
		.addOption(getFile());
	}
	
	public Option getHelp() {
		return Option.builder("h")
		.longOpt("help")
		.desc("Show this help message")
		.build();
	}
	
	public Option getCookieDomain() {
		return Option.builder()
		.longOpt("cookie-domain")
		.desc("Domain to set auth cookie on (required)")
		.hasArg()
		.required()
		.type(String.class)
		.build();
	}
	
	public Option getInsecureCookie() {
		return Option.builder("i")
		.longOpt("insecure-cookie")
		.desc("Use insecure cookies")
		.hasArg()
		.type(Boolean.class)
		.build();
	}
	
	public Option getAuthHost() {
		return Option.builder()
		.longOpt("auth-host")
		.desc("External hostname to access login page (required)")
		.hasArg()
		.required()
		.type(String.class)
		.build();
	}
	
	public Option getLifetime() {
		return Option.builder()
		.longOpt("lifetime")
		.desc("Lifetime in seconds (default: 86400)")
		.hasArg()
		.type(Integer.class)
		.build();
	}
	
	public Option getSecret() {
		return Option.builder()
		.longOpt("secret")
		.desc("Secret used for signing (required)")
		.hasArg()
		.required()
		.type(String.class)
		.build();
	}
	
	public Option getFile() {
		return Option.builder()
		.longOpt("file")
		.desc("Path & File name to use for htpasswd file (default: /htpasswd)")
		.hasArg()
		.type(String.class)
		.build();
	}
}
