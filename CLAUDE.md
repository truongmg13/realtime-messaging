# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Java 21 + Spring Boot 4 backend for 1-to-1 real-time messaging. The WebSocket server is a **from-scratch RFC 6455 implementation** on raw `java.net.ServerSocket` — no STOMP, no Spring WebSocket, no messaging framework. Spring Boot is used only for REST, Security (JWT), and JPA.

Two servers run in the same process, sharing one Spring `ApplicationContext` and JPA datasource:
- HTTP `:8080` — REST API (Spring MVC)
- TCP `:8081` — WebSocket (custom protocol, started via `RawWebSocketServer implements ApplicationRunner`)

## Commands

```bash
# Build
mvn clean package -q

# Run
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=MessageRouterTest

# Run a single test method
mvn test -Dtest=MessageRouterTest#route_recipientOnline_deliversAndMarksDelivered

# Docker
docker build -t realtime-messaging .
docker run -p 8080:8080 -p 8081:8081 -e JWT_SECRET=$(openssl rand -base64 32) -t realtime-messaging
```

`JWT_SECRET` is required (no default/hardcoded fallback — `app.jwt.secret: ${JWT_SECRET}` in `application.yml`).

## Architecture

### WebSocket connection lifecycle

Each accepted socket is wrapped in a `WebSocketConnection` (`websocket/server/`) and run on a fixed thread pool (`app.websocket.thread-pool-size`, one thread per connection — see `RawWebSocketServer`). Its internal state machine:

```
HANDSHAKING -> AUTHENTICATING -> OPEN -> CLOSED
```

- **HANDSHAKING**: `HandShakeParser`/`HandShakeResponder` (`websocket/handshake/`) perform the RFC 6455 HTTP upgrade.
- **AUTHENTICATING**: a `Timer` task enforces `app.websocket.auth-timeout` — if no `AUTH` frame arrives in time, the connection is closed with code `4001`.
- **OPEN**: reached once `ProtocolHandler.handleAuth` validates the JWT and calls `connection.updateAuthDetails(userId)`.
- Frame read/write goes through `FrameDecoder`/`FrameEncoder` (`websocket/frame/`), which implement RFC 6455 framing (masking, opcodes, etc.) directly.

### Protocol

Messages are a JSON envelope with a `type` field (`websocket/protocol/Envelope.java`, `MessageType.java`), dispatched in `ProtocolHandler`:
- `AUTH` — first message required after connect; carries the same JWT issued by the REST `/api/auth` endpoints (no separate WS auth flow). On success, registers a `WebSocketSession` in `SessionRegistry` and flushes any messages left in `SENT` status (delivered-while-offline case).
- `SEND` — requires an authenticated connection; persists the message via `MessageService`, then hands it to `MessageRouter`.

`MessageRouter.route()` looks up the recipient in `SessionRegistry` (a `ConcurrentHashMap<UUID, WebSocketSession>`, the single source of truth for who's online). If online, it delivers immediately and marks the message `DELIVERED`; if offline, the message simply stays `SENT` in the DB and is pushed on the recipient's next `AUTH` (see above) — there's no queue/broker involved.

### REST / Auth

- `AuthController` + `AuthService` handle register/login, issuing JWTs via `JwtUtil` (`security/`).
- `SecurityConfig` is stateless (`SessionCreationPolicy.STATELESS`), permits `/api/auth/**` and `/h2-console/**`, and requires auth on everything else. CORS currently allows all origins (`allowedOriginPatterns: "*"`) — tighten before production.
- Errors flow through `AppException` (carries an `HttpStatus`) and its subclasses (`BadRequestException`, `ConflictException`, `NotFoundException`, `UnauthorizedException`) in `exception/`, centrally translated to `ErrorResponse` JSON by `GlobalExceptionController` (`@RestControllerAdvice`).

### Persistence

H2 in-memory (`spring.jpa.hibernate.ddl-auto: create-drop`) — schema is recreated on every startup, nothing persists across restarts. Swappable for PostgreSQL by changing `spring.datasource`. Entities: `User`, `Message` (status: `SENT`/`DELIVERED`, see `MessageStatus`), `Attachment`.

### Attachments

`AttachmentService` validates and stores uploads behind the `AttachmentStorage` interface, with `LocalAttachmentStorage` writing to `app.attachment.upload-dir` (default `./uploads`) using the attachment UUID as the stored filename. This area is under active development on the current branch — check `AttachmentService`/`LocalAttachmentStorage` for TODOs before assuming upload is fully wired end-to-end.

## Key Design Decisions

| Decision | Choice | Rationale |
|---|---|---|
| WebSocket transport | Raw RFC 6455 over `java.net.ServerSocket` | No framework peer-to-peer messaging |
| Protocol | Custom JSON envelope with `type` field | Simple, debuggable, no STOMP overhead |
| Auth over WebSocket | First-message `AUTH` with JWT | Same JWT as REST, no separate auth flow |
| Offline delivery | Message stored as `SENT`, pushed on next `AUTH` | No queue infra needed for MVP |
| Thread model | One thread per connection (fixed pool of 100) | Simple |
| Persistence | H2 in-memory (swappable to PostgreSQL) | Zero-setup for MVP demo |

If scaling beyond a single node, `SessionRegistry` (in-memory map) would need to become Redis pub/sub or similar, since it's per-process.

## Manual testing

`websocket-client.html` at the repo root is a standalone browser client for exercising the WebSocket protocol by hand (connect, AUTH, SEND) against a locally running server. `docs/create_users.md` and `docs/request.http` have example curl/HTTP requests for the REST API.
