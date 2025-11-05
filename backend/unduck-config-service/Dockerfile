# Build Spring Boot JAR
FROM gradle:8.7-jdk17 AS builder
WORKDIR /workspace

# Gradle 설정 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# 소스 복사
COPY src ./src

# 빌드
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar


# Runtime
FROM eclipse-temurin:17-jre
WORKDIR /app

# 필수 유틸 설치 (curl)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# 빌드된 JAR 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

# JVM 옵션 (필요 시 조정 가능)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 포트 노출
EXPOSE 8888

# 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]