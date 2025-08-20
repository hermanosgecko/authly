#FROM openjdk:8-jre-alpine
#FROM eclipse-temurin:8-jre-ubi9-minimal
FROM eclipse-temurin:21-jre-ubi9-minimal


EXPOSE 4567/tcp

ENV COOKIE_NAME=authentik.token
ENV INSECURE_COOKIE=false 
ENV LIFETIME=86400
ENV FILE=htpasswd
ENV COOKIE_DOMAIN, AUTH_HOST, SECRET 

COPY target/*.jar /app.jar

WORKDIR /

CMD ["java","-jar", "/app.jar"]