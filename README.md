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
    command: [
    "--cookie-domain=docker.localhost", 
    "--auth-host=auth.docker.localhost", 
    "--insecure-cookie=true",
    "--secret=THIS_IS_A_SECRET"
    ] # Example assumes no https, do not use in production
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

The following configuration options are supported:
```
usage: Authentik [options]
    --auth-host <arg>         External hostname to access login page (required)
    --cookie-domain <arg>     Domain to set auth cookie on (required)
    --file <arg>              Path & File name to use for htpasswd file (default: /htpasswd)
 -h,--help                    Show this help message
 -i,--insecure-cookie <arg>   Use insecure cookies
    --lifetime <arg>          Lifetime in seconds (default: 86400)
    --secret <arg>            Secret used for signing (required)
```

### Option Details

- `auth-host`

  When a user accesses a restricted host they will be forwarded to this host to log in. 
  The host should be specified without protocol or path, for example:

   ```
   --auth-host="auth.example.com"
   ```

- `cookie-domain`

  When set, if a user successfully completes authentication, then if the host of the original request requiring authentication is a subdomain of a given cookie domain, then the authentication cookie will be set for the higher level cookie domain. This means that a cookie can allow access to multiple subdomains without re-authentication. 

   For example:
   ```
   --cookie-domain="example.com"  --cookie-domain="test.org"
   ```

   For example, if the cookie domain `test.com` has been set, and a request comes in on `app1.test.com`, following authentication the auth cookie will be set for the whole `test.com` domain. As such, if another request is forwarded for authentication from `app2.test.com`, the original cookie will be sent and so the request will be allowed without further authentication.

- `file`
  
  The path and filename for the users file to use.  The value should be relative to the container for example:

   ```
   --file="/htpasswd"
   ```
   
   and for the container:
   ```
   ${PWD}/hostpath/htpasswd:/containerpath/htpasswd
   ```
The default value is `/htpasswd`

- `insecure-cookie`

   If you are not using HTTPS between the client and traefik, you will need to pass the `insecure-cookie` option which will mean the `Secure` attribute on the cookie will not be set.

- `lifetime`

   How long a successful authentication session should last, in seconds.

   Default: `86400` (24 hours)

- `secret`

   Used to sign cookies authentication, should be a random (e.g. `openssl rand -hex 16`)
   
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
