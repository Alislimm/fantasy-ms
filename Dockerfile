# Stage 1: Build the JAR
FROM openjdk:17-jdk-slim AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean build -x test

# Stage 2: Run the JAR
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/fantasy-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
