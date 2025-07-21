package com.aimobs.network.application;

import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.network.MessageService;
import com.aimobs.network.core.ConnectionState;
import com.aimobs.network.core.NetworkMessage;
import com.aimobs.network.core.WebSocketConnection;
import com.aimobs.AiMobsMod;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Pure business logic coordinator - no external dependencies.
 * Following Feathers: separate the "what" from the "how".
 * This class defines WHAT should happen, not HOW it connects.
 */
public class NetworkCommandCoordinator implements WebSocketConnection.ConnectionListener {
    
    private final MessageService messageService;
    private ConnectionState connectionState;
    private String lastError;
    
    // Error recovery configuration
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000; // 1 second
    private static final long MAX_RETRY_DELAY_MS = 30000; // 30 seconds
    
    // Error recovery state
    private int currentRetryAttempts = 0;
    private long currentRetryDelay = INITIAL_RETRY_DELAY_MS;
    private final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor();
    private Runnable reconnectCallback;
    
    public NetworkCommandCoordinator(MessageService messageService) {
        this.messageService = messageService;
        this.connectionState = ConnectionState.DISCONNECTED;
    }
    
    /**
     * Handle incoming raw message - pure business logic.
     */
    public void handleIncomingMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            AiMobsMod.LOGGER.warn("Received empty message");
            return;
        }
        
        NetworkMessage message = messageService.parseMessage(rawMessage);
        
        if (message != null && messageService.validateMessage(message)) {
            AICommand command = messageService.convertToCommand(message);
            if (command != null) {
                messageService.queueCommand(command);
                AiMobsMod.LOGGER.debug("Queued command: " + message.getData().getAction());
            } else {
                AiMobsMod.LOGGER.warn("Failed to convert message to command: " + rawMessage);
            }
        } else {
            // Only log as warning if it's not a welcome/status message
            if (message != null && ("welcome".equals(message.getType()) || "status".equals(message.getType()))) {
                AiMobsMod.LOGGER.debug("Received non-command message: " + message.getType());
            } else {
                AiMobsMod.LOGGER.warn("Received invalid message: " + rawMessage);
            }
        }
    }
    
    /**
     * Prepare message for sending - pure business logic.
     */
    public String prepareOutgoingMessage(NetworkMessage message) {
        if (!messageService.validateMessage(message)) {
            return null;
        }
        
        // Convert to JSON format expected by server
        return String.format("""
            {
              "type": "%s",
              "timestamp": "%s",
              "data": {
                "action": "%s",
                "parameters": %s,
                "context": %s
              }
            }
            """, 
            message.getType(),
            message.getTimestamp(),
            message.getData().getAction(),
            formatMapAsJson(message.getData().getParameters()),
            formatMapAsJson(message.getData().getContext())
        );
    }
    
    public ConnectionState getConnectionState() {
        return connectionState;
    }
    
    public String getLastError() {
        return lastError;
    }
    
    public int getQueuedCommandCount() {
        return messageService.getQueuedCommandCount();
    }
    
    /**
     * Set callback for reconnection attempts.
     * This allows the service layer to trigger actual reconnections.
     */
    public void setReconnectCallback(Runnable reconnectCallback) {
        this.reconnectCallback = reconnectCallback;
    }
    
    /**
     * Reset retry state after successful connection.
     */
    public void resetRetryState() {
        currentRetryAttempts = 0;
        currentRetryDelay = INITIAL_RETRY_DELAY_MS;
    }
    
    /**
     * Initiate automatic reconnection with exponential backoff.
     */
    private void scheduleReconnection() {
        if (currentRetryAttempts >= MAX_RETRY_ATTEMPTS) {
            AiMobsMod.LOGGER.error("Maximum retry attempts reached. Giving up automatic reconnection.");
            connectionState = ConnectionState.ERROR;
            return;
        }
        
        connectionState = ConnectionState.RECONNECTING;
        currentRetryAttempts++;
        
        AiMobsMod.LOGGER.info("Scheduling reconnection attempt {} in {}ms", 
            currentRetryAttempts, currentRetryDelay);
        
        retryExecutor.schedule(() -> {
            if (reconnectCallback != null && connectionState == ConnectionState.RECONNECTING) {
                AiMobsMod.LOGGER.info("Attempting reconnection (attempt {}/{})", 
                    currentRetryAttempts, MAX_RETRY_ATTEMPTS);
                reconnectCallback.run();
            }
            
            // Exponential backoff with jitter
            currentRetryDelay = Math.min(currentRetryDelay * 2, MAX_RETRY_DELAY_MS);
        }, currentRetryDelay, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Shutdown retry executor and cleanup resources.
     */
    public void shutdown() {
        connectionState = ConnectionState.SHUTDOWN;
        retryExecutor.shutdown();
        try {
            if (!retryExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                retryExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            retryExecutor.shutdownNow();
        }
    }
    
    // WebSocketConnection.ConnectionListener implementations
    @Override
    public void onConnected() {
        connectionState = ConnectionState.CONNECTED;
        lastError = null;
        resetRetryState(); // Reset retry state on successful connection
        AiMobsMod.LOGGER.info("WebSocket connection established");
    }
    
    @Override
    public void onDisconnected() {
        // Only start reconnection if we're not in shutdown state
        if (connectionState != ConnectionState.SHUTDOWN) {
            AiMobsMod.LOGGER.info("WebSocket connection lost, scheduling reconnection");
            scheduleReconnection();
        } else {
            connectionState = ConnectionState.DISCONNECTED;
            AiMobsMod.LOGGER.info("WebSocket connection closed during shutdown");
        }
    }
    
    @Override
    public void onError(Exception error) {
        lastError = error.getMessage();
        AiMobsMod.LOGGER.error("WebSocket error: " + error.getMessage(), error);
        
        // Schedule reconnection for recoverable errors
        if (isRecoverableError(error)) {
            AiMobsMod.LOGGER.info("Error appears recoverable, scheduling reconnection");
            scheduleReconnection();
        } else {
            connectionState = ConnectionState.ERROR;
            AiMobsMod.LOGGER.error("Non-recoverable error, not attempting reconnection");
        }
    }
    
    /**
     * Determine if an error is recoverable and should trigger reconnection.
     */
    private boolean isRecoverableError(Exception error) {
        String message = error.getMessage();
        if (message == null) return true; // Unknown errors are considered recoverable
        
        // Network connectivity issues are recoverable
        return message.contains("Connection refused") ||
               message.contains("Connection timeout") ||
               message.contains("Network unreachable") ||
               message.contains("Connection reset");
    }
    
    @Override
    public void onMessageReceived(String message) {
        handleIncomingMessage(message);
    }
    
    private String formatMapAsJson(java.util.Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                sb.append("\"").append(entry.getValue()).append("\"");
            } else {
                sb.append(entry.getValue());
            }
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }
}