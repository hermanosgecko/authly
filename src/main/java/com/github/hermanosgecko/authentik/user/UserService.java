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
package com.github.hermanosgecko.authentik.user;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {

	private final Logger logger = LoggerFactory.getLogger(UserService.class);

	private String fileName;
	private File htpasswdFile;
	private volatile long lastModified;
	
    private final Map<String, String> htUsers = new ConcurrentHashMap<String, String>();

    /**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	/**
     * Authenticates the user.
     */
    public boolean authenticate(String username, String password) {
        boolean authenticated = false;
    	read();
        String storedPwd = htUsers.get(username);
        if (storedPwd != null) {
            // test Apache MD5 variant encrypted password
            if (storedPwd.startsWith("$apr1$")) {
                if (storedPwd.equals(Md5Crypt.apr1Crypt(password, storedPwd))) {
                    logger.debug("Apache MD5 encoded password matched for user '" + username + "'");
                    authenticated = true;
                }
            }
            // test unsalted SHA password
            else if (storedPwd.startsWith("{SHA}")) {
                String passwd64 = Base64.encodeBase64String(DigestUtils.sha1(password));
                if (storedPwd.substring("{SHA}".length()).equals(passwd64)) {
                    logger.debug("Unsalted SHA-1 encoded password matched for user '" + username + "'");
                    authenticated = true;
                }
            }
            // test libc crypt() encoded password
            else if (storedPwd.equals(Crypt.crypt(password, storedPwd))) {
                logger.debug("Libc crypt encoded password matched for user '" + username + "'");
                authenticated = true;
            }
            // test clear text
            else if (storedPwd.equals(password)){
                logger.debug("Clear text password matched for user '" + username + "'");
                authenticated = true;
            }

        }

        return authenticated;    
    }
    /**
     * Reads the password file and rebuilds the in-memory lookup tables.
     */
    protected synchronized void read() {
    	boolean forceReload = false;
    	File file = new File(fileName);
        if (!file.equals(htpasswdFile)) {
            this.htpasswdFile = file;
            this.htUsers.clear();
            forceReload = true;
        }

        if (htpasswdFile.exists() && (forceReload || (htpasswdFile.lastModified() != lastModified))) {
            lastModified = htpasswdFile.lastModified();
            htUsers.clear();

            Pattern entry = Pattern.compile("^([^:]+):(.+)");

            Scanner scanner = null;
            try {
                scanner = new Scanner(new FileInputStream(htpasswdFile));
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (!line.isEmpty() &&  !line.startsWith("#")) {
                        Matcher m = entry.matcher(line);
                        if (m.matches()) {
                            htUsers.put(m.group(1), m.group(2));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(MessageFormat.format("Failed to read {0}", htpasswdFile), e);
            } finally {
                if (scanner != null) {
                	scanner.close();
                }
            }
        }
    }
}
