# Stage 1: Build the app
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .

# 1. Fix Windows CRLF line endings (the hidden culprit!)
RUN sed -i 's/\r$//' gradlew

# 2. Ensure execute permissions
RUN chmod +x gradlew

# 3. Build the jar, skip tests to speed up deployment
RUN ./gradlew build -x test --no-daemon

# Stage 2: Run the app
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar from Stage 1
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]