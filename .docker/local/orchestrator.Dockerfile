# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace

# Copy all project files. In a multi-module project, the build context
# needs access to all modules to resolve dependencies.
COPY . .

# Build the specific 'orchestrator' module.
# The '-pl' flag selects the module, and '-am' builds its dependencies too.
# Skipping tests speeds up the Docker build.
RUN mvn -pl orchestrator/app -am clean package -DskipTests

# Stage 2: Create the final, lightweight runtime image
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy only the built JAR file from the build stage
COPY --from=build /workspace/orchestrator/app/target/*.jar app.jar

# Expose the default Spring Boot port and run the application
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]