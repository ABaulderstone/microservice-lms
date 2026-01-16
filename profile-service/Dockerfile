# =========================
# Build stage
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /build

RUN ls -la
# Copy pom.xml first (better caching)
COPY profile-service/pom.xml ./pom.xml

# Copy source code
COPY profile-service/src ./src

# Copy shared proto files
COPY proto ./proto

# Build (includes protobuf + gRPC codegen)
RUN mvn clean package -DskipTests -Pdocker


# =========================
# Runtime stage
# =========================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=build /build/target/profile-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]
