# Use the official OpenJDK image for Java 17
FROM openjdk:17-jdk-alpine AS build

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper and related files
COPY gradlew gradlew
COPY gradle gradle

# Copy the project files into the container
COPY . .

# Make the Gradle wrapper executable
RUN chmod +x gradlew

# Build the project using the Gradle wrapper
RUN ./gradlew build --no-daemon

# Use the official OpenJDK image to run the application
FROM openjdk:17-jdk-alpine

# Set the working directory in the container
WORKDIR /app

# Copy the jar file from the build image
COPY --from=build /app/build/libs/document-storage-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the jar file
CMD ["java", "-jar", "app.jar"]
