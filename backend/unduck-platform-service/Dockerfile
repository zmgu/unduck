#########################################
# 🔹 Stage 1 — Build Spring Boot JAR
#########################################
FROM gradle:8.5-jdk17-alpine AS builder
WORKDIR /workspace

# Gradle 캐시 복사(선택사항)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

# 나머지 프로젝트 복사
COPY . .

# 실행 권한 및 빌드
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar


#########################################
# 🔹 Stage 2 — Runtime
#########################################
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Config Server 연동 시 bootstrap.yml 누락 방지
COPY src/main/resources/ /app/resources/

# 빌드된 JAR 복사
COPY --from=builder /workspace/build/libs/*-SNAPSHOT.jar app.jar

# JVM 옵션(필요 시 환경변수로 대체 가능)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

# 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
