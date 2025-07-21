package com.aimobs.network.application;

import com.aimobs.network.WebSocketService;
import com.aimobs.network.core.ConnectionState;
import com.aimobs.network.core.NetworkMessage;
import com.aimobs.network.core.WebSocketConnection;
import com.aimobs.AiMobsMod;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

/**
 * Testable WebSocket service using dependency injection.
 * Following Feathers: dependencies are injected, making this easily testable.
 * The "seam" is the WebSocketConnection interface.
 */
public class TestableWebSocketService implements WebSocketService {
    
    private final WebSocketConnection connection;
    private final NetworkCommandCoordinator coordinator;
    
    // Message buffering for offline periods
    private final Queue<NetworkMessage> pendingMessages = new ConcurrentLinkedQueue<>();
    private static final int MAX_PENDING_MESSAGES = 100;
    
    private String currentServerUrl;
    
    public TestableWebSocketService(WebSocketConnection connection, NetworkCommandCoordinator coordinator) {
        this.connection = connection;
        this.coordinator = coordinator;
        
        // Set up reconnection callback
        coordinator.setReconnectCallback(this::attemptReconnection);
    }
    
    @Override
    public void connect(String serverUrl) {
        this.currentServerUrl = serverUrl;
        connection.connect(serverUrl, coordinator);
    }
    
    /**
     * Attempt reconnection using the stored server URL.
     * Called by the coordinator's retry mechanism.
     */
    private void attemptReconnection() {
        if (currentServerUrl != null) {
            connection.connect(currentServerUrl, coordinator);
        }
    }
    
    @Override
    public void disconnect() {
        connection.disconnect();
    }
    
    @Override
    public ConnectionState getConnectionState() {
        return coordinator.getConnectionState();
    }
    
    @Override
    public boolean isConnected() {
        return connection.isConnected();
    }
    
    @Override
    public void sendMessage(NetworkMessage message) {
        if (connection.isConnected()) {
            // Connection is active - send immediately and flush any pending messages
            sendMessageNow(message);
            flushPendingMessages();
        } else {
            // Connection is down - buffer message for later
            bufferMessage(message);
        }
    }
    
    /**
     * Send a message immediately through the connection.
     */
    private void sendMessageNow(NetworkMessage message) {
        String jsonMessage = coordinator.prepareOutgoingMessage(message);
        if (jsonMessage != null) {
            connection.sendMessage(jsonMessage);
        }
    }
    
    /**
     * Buffer a message for sending when connection is restored.
     */
    private void bufferMessage(NetworkMessage message) {
        if (pendingMessages.size() >= MAX_PENDING_MESSAGES) {
            // Remove oldest message to make room
            NetworkMessage oldest = pendingMessages.poll();
            if (oldest != null) {
                AiMobsMod.LOGGER.warn("Message buffer full, dropping oldest message");
            }
        }
        
        pendingMessages.offer(message);
        AiMobsMod.LOGGER.debug("Buffered message for later delivery (queue size: {})", 
            pendingMessages.size());
    }
    
    /**
     * Send all buffered messages when connection is restored.
     */
    private void flushPendingMessages() {
        if (pendingMessages.isEmpty()) {
            return;
        }
        
        AiMobsMod.LOGGER.info("Flushing {} buffered messages", pendingMessages.size());
        
        NetworkMessage message;
        while ((message = pendingMessages.poll()) != null) {
            sendMessageNow(message);
        }
    }
    
    @Override
    public void handleIncomingMessage(String rawMessage) {
        coordinator.handleIncomingMessage(rawMessage);
    }
    
    @Override
    public void shutdown() {
        // Mark as shutdown to prevent reconnection attempts
        coordinator.shutdown();
        disconnect();
        connection.cleanup();
        
        // Clear pending messages
        pendingMessages.clear();
    }
}