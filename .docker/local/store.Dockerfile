# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Copy all project files to ensure all modules are available for the build
COPY . .

# Build the 'store' module and its dependencies
RUN mvn -pl store/app -am clean package -DskipTests

# Stage 2: Create the final, lightweight runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /workspace/store/app/target/*.jar app.jar

# Run the application on a random available port
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=0"]