# realtime-messaging
Realtime message app

## Project Overview
Java 21 + Spring Boot 4 backend for 1-to-1 real-time messaging.
Custom WebSocket server implements RFC 6455 from scratch - no STOMP broker, no messaging framework.
Spring Boot handles REST, Security (JWT), and JPA only.

## Build docker image
docker build -t truongmg13/realtime-messaging .

## Run docker image
docker run -p 8080:8080 -p 8081:8081 -t truongmg13/realtime-messaging