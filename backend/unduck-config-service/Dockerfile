#########################################
# 🔹 Stage 1 — Build Spring Boot JAR
#########################################
FROM gradle:8.7.0-jdk17-jammy AS builder
WORKDIR /workspace

# Gradle 설정 복사
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# 소스 복사
COPY src ./src

# 빌드
RUN chmod +x gradlew && ./gradlew --no-daemon clean bootJar


#########################################
# 🔹 Stage 2 — Runtime
#########################################
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# ✅ 필수 유틸 설치 (curl, grep)
RUN apk add --no-cache curl grep

# 빌드된 JAR 복사
COPY --from=builder /workspace/build/libs/*.jar app.jar

# JVM 옵션 (필요 시 조정 가능)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# 포트 노출
EXPOSE 8888

# ✅ Healthcheck 용도로 curl 동작 확인
HEALTHCHECK --interval=10s --timeout=5s --retries=15 --start-period=100s \
  CMD test "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8888/actuator/health)" = "200"


# 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
