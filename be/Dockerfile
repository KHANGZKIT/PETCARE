# ============================================================
# Stage 1: Build application with Maven and Eclipse Temurin JDK 25
# ============================================================
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copy pom.xml and pre-fetch dependencies to leverage Docker cache
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests

# ============================================================
# Stage 2: Lightweight runtime environment with JRE 25
# ============================================================
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the generated JAR file from the builder stage
COPY --from=builder /build/target/petcare-backend-*.jar app.jar

# Expose the default application port
EXPOSE 8080

# Set default environment variables
ENV SERVER_PORT=8080

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
