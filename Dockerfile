FROM openjdk:8-jre-alpine

EXPOSE 4567/tcp

COPY target/*.jar /app.jar

WORKDIR /

ENTRYPOINT ["/usr/bin/java","-jar", "/app.jar"]