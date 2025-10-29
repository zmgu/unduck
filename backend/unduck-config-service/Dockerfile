FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/unduck-config-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
