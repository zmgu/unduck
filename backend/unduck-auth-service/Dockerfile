# Build Spring Boot JAR
FROM gradle:8.7-jdk17 AS builder
WORKDIR /workspace

# Gradle 캐시 및 설정 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 나머지 프로젝트 복사 및 빌드
COPY . .
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar


# Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# Config Server 연동 시 bootstrap.yml 포함
COPY src/main/resources/ /app/resources/

# 빌드된 JAR 복사
COPY --from=builder /workspace/build/libs/*-SNAPSHOT.jar app.jar

# JVM 설정
ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]