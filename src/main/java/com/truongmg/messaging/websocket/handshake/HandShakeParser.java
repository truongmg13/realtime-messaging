package com.truongmg.messaging.websocket.handshake;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads on HTTP/1.1 WebSocket upgrade request from a raw InputStream
 */
@Slf4j
public class HandShakeParser {

    private static final int MAX_HEADER_SIZE = 8192;

    private final String method;
    private final String path;
    private final Map<String, String> headers;

    private HandShakeParser(String method, String path, Map<String, String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }

    public static HandShakeParser parse(InputStream in) throws IOException {
        String requestLine = readLine(in);
        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Malformed request line: " + requestLine);
        }

        String method = parts[0];
        String path = parts[1];
        String protocol = parts[2];
        log.info("Parsed method: {}, path: {}, protocol: {}", method, path, protocol);

        Map<String, String> headers = new HashMap<>();
        String line;
        while (!(line = readLine(in)).isEmpty()) {
            int colon = line.indexOf(":");
            if (colon > 0) {
                String name = line.substring(0, colon).trim().toLowerCase();
                String value = line.substring(colon + 1).trim();
                headers.put(name, value);
            }
        }

        log.info("Parsed headers: {}", headers);
        validateUpgradeRequest(method, path, protocol, headers);

        return new HandShakeParser(method, path, headers);
    }

    private static void validateUpgradeRequest(String method, String path, String protocol, Map<String, String> headers) {
        if (!"GET".equals(method)) {
            throw new IllegalArgumentException("Invalid HTTP method for WebSocket upgrade: " + method);
        }
        if (!"HTTP/1.1".equals(protocol)) {
            throw new IllegalArgumentException("Invalid HTTP protocol version for WebSocket upgrade: " + protocol);
        }

        String upgrade = headers.getOrDefault("upgrade", "");
        String connection = headers.getOrDefault("connection", "");
        String key = headers.getOrDefault("sec-websocket-key", "");

        if (!"websocket".equalsIgnoreCase(upgrade)) {
            throw new IllegalArgumentException("Not a websocket upgrade request");
        }
        if (!"Upgrade".equalsIgnoreCase(connection)) {
            throw new IllegalArgumentException("Missing Connection: Upgrade header");
        }
        if (key.isEmpty()) {
            throw new IllegalArgumentException("Missing Sec-WebSocket-Key header");
        }
    }

    private static String readLine(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int totalRead = 0;
        int b;

        while ((b = in.read()) != -1) {
            if (++totalRead > MAX_HEADER_SIZE) {
                throw new IOException("HTTP header line exceeds limit");
            }

            if (b == '\r') {
                int next = in.read();
                if (next == '\n') break;
                sb.append((char) b);
            } else if (b == '\n') {
                break;
            } else {
                sb.append((char) b);
            }
        }

        return sb.toString();
    }

    public String getWebSocketKey() {
        return headers.get("sec-websocket-key");
    }
}
