# Use the Amazon Corretto 1.8 (Java 8) image as the base
FROM amazoncorretto:8

# Set the working directory inside the container
WORKDIR /app

# Copy the application JAR file to the container
#COPY leaves-0.0.1-SNAPSHOT.jar .

# Expose the port used by the Java application (if applicable)
EXPOSE 8080

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db_host:5432/leaves_db
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=1234

# Run the Java application
CMD ["java", "-jar", "leaves-0.0.1-SNAPSHOT.jar"]