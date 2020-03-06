# authentik [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) ![Java CI](https://github.com/hermanosgecko/authentik/workflows/Java%20CI/badge.svg) [![Coverage Status](https://coveralls.io/repos/github/hermanosgecko/authentik/badge.svg?branch=master)](https://coveralls.io/github/hermanosgecko/authentik?branch=master)  ![Docker Pulls](https://img.shields.io/docker/pulls/hermanosgecko/authentik.svg)  [![GitHub release](https://img.shields.io/github/release/hermanosgecko/authentik.svg)](https://Github.com/hermanosgecko/authentik/releases/)
A more minimal forward authentication service that provides login and authentication using an Apache basic authentication ([htpasswd](https://httpd.apache.org/docs/current/programs/htpasswd.html)) file for the [traefik](https://github.com/containous/traefik) reverse proxy/load balancer.

Based on the concept for [traefik-forward-auth](https://github.com/thomseddon/traefik-forward-auth) but uses a file based provider in place of google/OIDC.

## Usage

#### Simple:

See below for instructions on how to setup:

docker-compose.yml:

```yaml
version: '3'

services:
  traefik:
    image: traefik:1.7
    command: [
    "--web", 
    "--docker", 
    "--docker.domain=docker.localhost"
    ]
    depends_on:
    - "authentik"
    ports:
    - "80:80"
    volumes:
    - /var/run/docker.sock:/var/run/docker.sock:ro
    networks:
      traefik-net:
      public-net:
    labels:
    - "traefik.port=8080"
    - "traefik.frontend.rule=Host:traefik.docker.localhost"
    - "traefik.frontend.auth.forward.address=http://authentik:4567/auth"
    - "traefik.frontend.auth.forward.trustForwardHeader=true"
    - "traefik.frontend.auth.forward.authResponseHeaders=X-Forwarded-User"
    
  authentik:
    image: hermanosgecko/authentik:latest
    environment:
     - INSECURE_COOKIE=true  # Example assumes no https, do not use in production
     - COOKIE_DOMAIN=docker.localhost
     - AUTH_HOST=auth.docker.localhost 
     - SECRET=THIS_IS_A_SECRET 
    networks:
      traefik-net:
        aliases:
        - "authentik"
    volumes:
     - ${PWD}/htpasswd:/htpasswd:ro
    labels:
    - "traefik.port=4567"
    - "traefik.frontend.rule=Host:auth.docker.localhost"
    - "traefik.frontend.auth.forward.address=http://authentik:4567/auth"
    - "traefik.frontend.auth.forward.trustForwardHeader=true"
    - "traefik.frontend.auth.forward.authResponseHeaders=X-Forwarded-User"

  whoami:
    image: stefanscherer/whoami:latest
    networks:
      traefik-net:
    labels:
    - "traefik.port=8080"
    - "traefik.frontend.rule=Host:whoami.docker.localhost"
    - "traefik.frontend.auth.forward.address=http://authentik:4567/auth"
    - "traefik.frontend.auth.forward.trustForwardHeader=true"
    - "traefik.frontend.auth.forward.authResponseHeaders=X-Forwarded-User"


networks:
  traefik-net:
    internal: true
  public-net:
```
## Configuration

### Overview
## Parameters

Container images are configured using parameters passed at runtime (such as those above). These parameters are separated by a colon and indicate `<external>:<internal>` respectively. For example, `-p 8080:80` would expose port `80` from inside the container to be accessible from the host's IP on port `8080` outside the container.

| Parameter | Function |
| :----: | --- |
| `-p 4567` | The port for the authentik webinterface |
| `-e COOKIE_DOMAIN=mydomain.com` | Domain for the cookie. This is required |
| `-e AUTH_HOST=auth.mydomain.com` | Sub domain to access the login page for authentik, this must be a sub domain of the `COOKIE_DOMAIN` and should be specified without protocol or path.  This is required  |
| `-e SECRET=THIS_IS_A_SECRET` | Used to sign cookies authentication, should be a random (e.g. `openssl rand -hex 16`).  This is required  |
| `-e COOKIE_NAME=tokenname` | Specify a cookie name. Defaulted to `authentik.token` |
| `-e INSECURE_COOKIE=true` | If you are not using HTTPS between the client and traefik, you will need to pass this which will mean the `Secure` attribute on the cookie will not be set. Defaulted to `False`. |
| `-e LIFETIME=86400` | How long a successful authentication session should last, in seconds. Defaulted to `86400` (24 hours) |
| `-v /htpasswd` | Location of the htpasswd file|
   
 ## Concepts

### Users file

You restrict users who can login with by adding them to the [htpasswd](https://httpd.apache.org/docs/current/programs/htpasswd.html) file.

Supported password formats are a Apache MD5, SHA1, libc crypt or plain text. Files may contain a mixture of different encoding types of passwords; some user records may have plain text or MD5-encrypted passwords while others in the same file may have passwords encrypted with libc crypt.

### Forwarded Headers

The authenticated user is set in the `X-Forwarded-User` header, to pass this on add this to the `authResponseHeaders` config option in traefik, as shown [here](https://github.com/hermanosgecko/authentik/blob/master/docker-compose.yml).

### Operation Mode

The user flow will be:

1. Request to `app10.test.com/home/page`
2. User redirected to login page
3. User is validated, auth cookie is set to `test.com`
4. User is redirected to `app10.test.com/home/page`
5. Request is allowed

Please note: You must ensure that `auth-host`  is a subdomain of `cookie-domain` and requests to your `auth-host` are routed to the authentik container, as demonstrated with the service labels in the [docker-compose.yml](https://github.com/hermanosgecko/authentik/blob/master/docker-compose.yml) example.
## License

[Apache 2.0](https://github.com/hermanosgecko/authentik/blob/master/LICENSE)
