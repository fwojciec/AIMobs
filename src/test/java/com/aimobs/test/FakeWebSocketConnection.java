package com.aimobs.test;

import com.aimobs.network.core.WebSocketConnection;

import java.util.ArrayList;
import java.util.List;

/**
 * Test double for WebSocket connections.
 * Following Feathers: this is our seam that lets us control behavior in tests.
 * No external dependencies, fully controllable, deterministic.
 */
public class FakeWebSocketConnection implements WebSocketConnection {
    
    private boolean connected = false;
    private ConnectionListener listener;
    private final List<String> sentMessages = new ArrayList<>();
    private boolean shouldFailConnection = false;
    private boolean shouldFailSending = false;
    
    @Override
    public void connect(String serverUrl, ConnectionListener listener) {
        this.listener = listener;
        
        if (shouldFailConnection) {
            if (listener != null) {
                listener.onError(new RuntimeException("Simulated connection failure"));
            }
            return;
        }
        
        connected = true;
        if (listener != null) {
            listener.onConnected();
        }
    }
    
    @Override
    public void disconnect() {
        if (connected) {
            connected = false;
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }
    
    @Override
    public boolean isConnected() {
        return connected;
    }
    
    @Override
    public void sendMessage(String message) {
        if (!connected) {
            throw new IllegalStateException("Not connected");
        }
        
        if (shouldFailSending) {
            if (listener != null) {
                listener.onError(new RuntimeException("Simulated send failure"));
            }
            return;
        }
        
        sentMessages.add(message);
    }
    
    @Override
    public void cleanup() {
        connected = false;
        listener = null;
        sentMessages.clear();
    }
    
    // Test control methods
    public void simulateIncomingMessage(String message) {
        if (connected && listener != null) {
            listener.onMessageReceived(message);
        }
    }
    
    public void simulateDisconnection() {
        if (connected) {
            connected = false;
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }
    
    public void simulateError(Exception error) {
        if (listener != null) {
            listener.onError(error);
        }
    }
    
    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }
    
    public void setConnectionFailure(boolean shouldFail) {
        this.shouldFailConnection = shouldFail;
    }
    
    public void setSendingFailure(boolean shouldFail) {
        this.shouldFailSending = shouldFail;
    }
    
    public void reset() {
        connected = false;
        listener = null;
        sentMessages.clear();
        shouldFailConnection = false;
        shouldFailSending = false;
    }
}