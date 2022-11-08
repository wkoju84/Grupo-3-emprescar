FROM openjdk:17-jdk-alpine3.14
WORKDIR /app
EXPOSE 8080
COPY target/emprescar.jar /app/app.jar
ENTRYPOINT ["java","-jar", "app.jar"]
#"-Dspring.profiles.active=docker",