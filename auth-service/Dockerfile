# =========================
# Build stage
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /build

# Copy Maven config first (better caching)
COPY pom.xml .
COPY src ./src

# Copy shared proto files into the service
COPY ../../proto ./proto

# Build the application (includes protobuf + gRPC codegen)
RUN mvn clean package -DskipTests


# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /build/target/auth-service.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
