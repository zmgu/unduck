# Build Spring Boot JAR
FROM gradle:8.7-jdk17 AS builder
WORKDIR /workspace

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
COPY . .

RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar

# Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY src/main/resources/ /app/resources/
COPY --from=builder /workspace/build/libs/*-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]