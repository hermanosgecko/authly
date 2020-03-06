FROM openjdk:8-jre-alpine

EXPOSE 4567/tcp

ENV COOKIE_NAME=authentik.token
ENV INSECURE_COOKIE=false 
ENV LIFETIME=86400
ENV FILE=htpasswd
ENV COOKIE_DOMAIN, AUTH_HOST, SECRET 

COPY target/*.jar /app.jar

WORKDIR /

ENTRYPOINT ["/usr/bin/java","-jar", "/app.jar"]