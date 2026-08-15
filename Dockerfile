# -- Stage 1: Build ----
FROM maven:3.9.7-eclipse-temurin-21 AS builder

WORKDIR /app

# Cache dependency layer separately
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -DskipTests -q

# -- Stage 2: Runtime --
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-root user
RUN addgroup -S messaging && adduser -S messaging -G messaging
USER messaging

COPY --from=builder /app/target/realtime-messaging-0.0.1-SNAPSHOT.jar app.jar

# HTTP (Spring Boot REST) + TCP (WebSocket)
EXPOSE 8080 8081

ENTRYPOINT ["java", "-jar", "app.jar"]