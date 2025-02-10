FROM openjdk:17-alpine
WORKDIR /app
COPY target/lettrage-0.0.1-SNAPSHOT.jar /app/lettrage-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT  ["java", "-jar", "lettrage-0.0.1-SNAPSHOT.jar"]