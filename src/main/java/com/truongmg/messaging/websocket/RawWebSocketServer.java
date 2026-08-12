package com.truongmg.messaging.websocket;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class RawWebSocketServer implements ApplicationRunner {

    @Value("${app.websocket.port}")
    private int port;

    @Value("${app.websocket.thread-pool-size}")
    private int threadPoolSize;

    private ServerSocket serverSocket;
    private ExecutorService connectionPool;
    private Thread acceptThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!running.compareAndSet(false, true)) return;
        log.info("Starting RawWebSocketServer");

        try {
            serverSocket = new ServerSocket(port);
            connectionPool = Executors.newFixedThreadPool(threadPoolSize, r -> {
                Thread t = new Thread(r, "ws-connection-" + System.nanoTime());
                t.setDaemon(true);
                return t;
            });

            acceptThread = new Thread(this::acceptLoop, "ws-accept");
            acceptThread.setDaemon(true);
            acceptThread.start();
        } catch (IOException ex) {
            log.error("Failed to start Websocket server on port {}: {}", port, ex.getMessage());
        }

    }

    private void acceptLoop() {
        while (running.get()) {
            try {
                log.info("Accepting connection");
                Socket socket = serverSocket.accept();

                WebSocketConnection connection = new WebSocketConnection(socket);
                connectionPool.submit(connection);

            } catch (IOException e) {
                log.error("Accept loop error: {}", e.getMessage(), e);
            }
        }
        log.info("WebSocket accept look terminated");
    }

    @PreDestroy
    public void stop() {
        if (!running.compareAndSet(true, false)) return;

        log.info("Stopping RawWebSocketServer");

        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            log.error("Error closing server socket: {}", e.getMessage(), e);
        }

        if (connectionPool != null) {
            connectionPool.shutdown();
        }

        log.info("WebSocket stop look terminated");
    }
}
