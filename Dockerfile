FROM openjdk:8-jdk
WORKDIR /app
ARG JAR_FILE=hamsterhub-application/target/*.jar
COPY ${JAR_FILE} app.jar
COPY assets/default.png assets/default.png
ENTRYPOINT ["java", "-jar", "app.jar"]