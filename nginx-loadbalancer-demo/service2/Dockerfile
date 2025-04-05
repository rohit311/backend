FROM openjdk:17-jdk-slim-buster
MAINTAINER marcbarbosa
COPY /target/app2-0.0.1-SNAPSHOT.jar /app/app.jar
WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
