# Use the Amazon Corretto 1.8 (Java 8) image as the base
FROM amazoncorretto:8

# Set the working directory inside the container
WORKDIR /home/app

RUN mkdir -p /home/app/certificates

COPY certificates/vacantion/service/service.vacation.lightsoftbulgaria.jks /home/app/certificates/service.vacation.lightsoftbulgaria.jks

# Copy the application JAR file to the container
COPY target/leaves-*.jar /home/app/app.jar

# Expose the port used by the Java application (if applicable)
EXPOSE 443

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db_host:5432/leaves_db
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=1234
ENV HOST_ADDRESS=localhost
ENV SERVER_PORT=443

# Run the Java application
CMD ["java", "-jar", "/home/app/app.jar"]
