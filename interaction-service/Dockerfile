# ── Stage 1: Build ─────────────────────────
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /build

# copy entire project (parent + modules)
COPY . .

# build only this module
RUN mvn -pl api-gateway -am clean package -DskipTests

# ── Stage 2: Runtime ───────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /build/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]