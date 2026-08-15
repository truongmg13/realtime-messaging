# Realtime Messaging

1-1 private Realtime message backend built with Spring Boot and a **custom WebSocket server**

## Project Overview

Java 21 + Spring Boot 4 backend for 1-to-1 real-time messaging.
Custom WebSocket server implements RFC 6455 from scratch - no STOMP broker, no messaging framework.
Spring Boot handles REST, Security (JWT), and JPA only.

## Architecture

```
Browser / Client
---- HTTP  :8080 -> REST API  (Spring Boot)
---- TCP   :8081 -> WebSocket (custom RFC 6455 implementation)
```

Both servers share the same Spring ApplicationContext and JPA datasource.

## Quick Start

**Prerequisites:** Java 21+, Maven 3.9+

```bash
# Clone and build
mvn clean package -q

# Run
mvn spring-boot:run

# Run tests only
mvn test
```

Server starts on:
- REST API -> http://localhost:8080
- WebSocket -> ws://localhost:8081
- 

## Build docker image

```bash
docker build -t realtime-messaging .
```

## Run docker image

```bash
docker run -p 8080:8080 -p 8081:8081 -e JWT_SECRET=$(openssl rand -base64 32) -t realtime-messaging
```
