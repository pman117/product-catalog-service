# =============================================================================
# Dockerfile — product-catalog-service
# Java 21 / Spring Boot 3 / Gradle (Kotlin DSL: build.gradle.kts)
# Multi-stage build: (1) build the jar with Gradle, (2) run it on a slim JRE
#
# PLACE THIS FILE AT THE GRADLE PROJECT ROOT — the same directory that
# contains build.gradle.kts, settings.gradle.kts, and gradlew. See the
# companion "Docker-CI-Placement-Diagnostic-Blueprint" document for exactly
# which directory that is in your specific project layout.
# =============================================================================

# ---- Stage 1: Build ----
FROM gradle:8.10-jdk21-alpine AS build

WORKDIR /app

# Copy only the Gradle wrapper and build files first, so Docker can cache
# the dependency-download layer separately from your source code. This
# project uses the Kotlin DSL (.kts), NOT Groovy — the filenames matter.
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

# Pre-download dependencies into the Gradle cache (cached layer; only
# re-runs when build.gradle.kts or settings.gradle.kts change).
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# Now copy the actual source code.
COPY src src

# Build the application JAR. Tests already ran in CI before this image is
# built, so skip them here to keep image builds fast.
RUN ./gradlew clean bootJar --no-daemon -x test

# ---- Stage 2: Run ----
FROM eclipse-temurin:21-jre-alpine AS runtime

# Run as non-root — baseline container security practice.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy only the built JAR — none of the Gradle cache, source, or build
# tools end up in the final runtime image.
COPY --from=build --chown=spring:spring /app/build/libs/*.jar app.jar

EXPOSE 8080

# Requires spring-boot-starter-actuator with the health endpoint enabled
# (add this dependency if not already present — you'll need it for
# Phase 2A/Micrometer anyway).
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# MaxRAMPercentage lets the JVM size its heap correctly relative to whatever
# memory limit the container is given (matters once this runs under
# ECS/Kubernetes with a memory limit set).
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

# =============================================================================
# BUILD (run from the Gradle project root — where this file lives):
#   docker build -t product-catalog-service:latest .
#
# RUN (standalone, against a MySQL container on the same Docker network):
#   docker network create catalog-net
#   docker run -d --name mysql --network catalog-net \
#     -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=product_catalog \
#     -p 3306:3306 mysql:8.1
#   docker run -d --name catalog-service --network catalog-net \
#     -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/product_catalog \
#     -e SPRING_DATASOURCE_USERNAME=root \
#     -e SPRING_DATASOURCE_PASSWORD=root \
#     -p 8080:8080 product-catalog-service:latest
#
# VERIFY:
#   curl http://localhost:8080/actuator/health
#   curl http://localhost:8080/api/v1/products
#
# NOTE ON SECRETS: plain -e flags above are for local smoke testing only.
# Never bake real credentials into the image. Use a git-ignored .env file
# locally, and a secrets manager (AWS Secrets Manager / HashiCorp Vault) in
# any real deployment.
# =============================================================================
