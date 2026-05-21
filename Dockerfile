# ── Stage 1: Build ───────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 先复制 pom 利用 Docker layer cache 缓存依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn package -DskipTests -B -q

# ── Stage 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# 安全：非 root 运行
RUN addgroup -S app && adduser -S app -G app

# curl 用于 healthcheck
RUN apk add --no-cache curl

WORKDIR /app
COPY --from=build /app/target/property-backend-*.jar app.jar
RUN chown app:app app.jar

USER app
EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+UseStringDeduplication \
  -Djava.security.egd=file:/dev/./urandom \
  -Duser.timezone=Asia/Shanghai \
  -Dfile.encoding=UTF-8"

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -sf http://localhost:8080/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
