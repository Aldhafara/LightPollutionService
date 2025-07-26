FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the JAR
COPY target/*.jar app.jar
COPY src/main/resources/data/viirs_vnl/vnl/2023/average.tiff /app/data/viirs_vnl/vnl/2023/average.tiff

# Expose standard Spring Boot port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
