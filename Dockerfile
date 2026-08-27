# Build stage
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts ./
COPY gradlew ./
COPY gradle/ gradle/
RUN chmod +x gradlew
RUN --mount=type=cache,target=/home/gradle/.gradle \
    --mount=type=cache,target=/home/gradle/.m2 \
    ./gradlew --no-daemon clean build -x ktlintCheck

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
RUN apk add --no-cache curl
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]