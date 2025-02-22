# Maven image to build Spring Boot app
FROM maven:3.8.4-openjdk-17 AS build

# Set the working directory
WORKDIR /app

# Copy pom.xml and install dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# OpenJDK image to run application
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built jar file from build stage
COPY --from=build /app/target/sftp-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

# Specify the command to run the appliation
ENTRYPOINT ["java", "-jar", "/app/sftp-0.0.1-SNAPSHOT.jar"]