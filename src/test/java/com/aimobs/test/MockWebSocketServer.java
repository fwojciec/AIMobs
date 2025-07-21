package com.aimobs.test;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Mock WebSocket server for testing network functionality.
 * Provides controllable test scenarios without external dependencies.
 */
public class MockWebSocketServer extends WebSocketServer {
    
    private final List<String> receivedMessages = new ArrayList<>();
    private final List<WebSocket> connections = new ArrayList<>();
    private CountDownLatch connectionLatch = new CountDownLatch(1);
    private CountDownLatch messageLatch = new CountDownLatch(1);
    private boolean shouldSimulateDisconnection = false;
    
    public MockWebSocketServer(int port) {
        super(new InetSocketAddress(port));
    }
    
    public MockWebSocketServer() {
        // Use port 0 for automatic port assignment
        super(new InetSocketAddress(0));
    }
    
    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        connections.add(conn);
        connectionLatch.countDown();
    }
    
    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
    }
    
    @Override
    public void onMessage(WebSocket conn, String message) {
        receivedMessages.add(message);
        messageLatch.countDown();
    }
    
    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }
    
    @Override
    public void onStart() {
        // Server started
    }
    
    public void sendToAll(String message) {
        for (WebSocket conn : connections) {
            conn.send(message);
        }
    }
    
    public void simulateDisconnection() {
        shouldSimulateDisconnection = true;
        for (WebSocket conn : connections) {
            conn.close();
        }
    }
    
    public boolean waitForConnection(long timeout, TimeUnit unit) {
        try {
            return connectionLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    public boolean waitForMessage(long timeout, TimeUnit unit) {
        try {
            return messageLatch.await(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    public List<String> getReceivedMessages() {
        return new ArrayList<>(receivedMessages);
    }
    
    public int getConnectionCount() {
        return connections.size();
    }
    
    public void reset() {
        receivedMessages.clear();
        connectionLatch = new CountDownLatch(1);
        messageLatch = new CountDownLatch(1);
        shouldSimulateDisconnection = false;
    }
}