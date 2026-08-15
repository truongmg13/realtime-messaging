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


## Project Structure
```
src/main/java/com/messaging/
-- MessagingApplication.java
-- config/
   -- JacksonConfig.java            # ObjectMapper bean
   -- SecurityConfig.java           # AUth, CORS, BCrypt
-- model/                           
-- repository/                      
-- dto/
-- exception/
-- security/
-- service/
-- controller/
-- websocket/
   -- frame/                        # WebSocketFrame, FrameDecoder, FrameEncoder
   -- handshake/                    # HandshakeParser, HandshareResponder
   -- session/                      # WebSocketSession, SessionRegistry
   -- protocol/                     # ProtocolHandler
   -- routing/                      # MessageRouter
   -- server/                       # WebSocketConnection, RawWebSocketServer
```

## Key Design Decisions
|Decision|Choice|Rational|
|---|---|---|
|WebSocket transport| Raw RFC 6455 over `java.net.ServerSocket` | No framework peer-to-peer messaging|
|Protocol | Custom JSON envelope with `type` field | Simple, debuggable, no STOMP overhead |
|Auth over WebSocket| First-message Auth with JWT | Same JWT as REST, no separate auth flow|
|Offline delivery | Message stored as `SENT`, pushed on next `AUTH`| No queue infra needed for MVP|
|Thread model | One thread per connection (fix pool of 100) | Simple |
|Persistence | H2 in-memory (can swap to PostgreSQL) | Zero-setup for MVP demo| 

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

## REST API

### Auth 

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{ "username": "bob", "password": "test12345678", "displayName": "Bob" }'
  
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{ "username": "bob", "password": "test12345678" }'
```

## WebSocket Protocol

Connect to `ws://localhost:8081` with any standard WebSocket client. The first message **must** be `AUTH` within 30 seconds.

### Protocol messages (JSON text frames)

**Client -> Server**

```json
// 1. Authenicate immediately after connect
{"type": "AUTH", "token": "eyJ..."}

// 2. Send a message
{"type": "SEND", "recipientId": "<uuid>", "content": "Hello!" }
```

**Server -> Client**

```json
// Auth confirmed
{"type": "AUTH_OK", "user_id": "<uuid>"}

// Incoming message
{"type": "MESSAGE", "id": "<uuid>", "senderId": <uuid>, "senderUsername":"", "content": "Hello!" }
```

## Production Checklist

- [ ] Replace `app.jwt.secret` with a securely generated 256-bit key (`openssl rand -base64 32`)
- [ ] Switch `spring.datasource` to PostgreSQL
- [ ] Restrict CORS `allowedOriginPatterns` to your frontend domain
- [ ] Add HTTPS / WSS via a reverse proxy (nginx) in front of both ports
- [ ] For multi-node: replace `SessionRegistry` with Redis pub/sub routing
- [ ] Tune `app.websocket.thread-pool-size` based on expected concurrent users