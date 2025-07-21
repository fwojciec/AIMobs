package com.aimobs.network;

import com.aimobs.network.core.ConnectionState;
import com.aimobs.network.core.NetworkMessage;

/**
 * Root interface defining WebSocket connection management contract.
 * Following Standard Package Layout - this is a service contract.
 */
public interface WebSocketService {
    
    /**
     * Establish connection to WebSocket server.
     * @param serverUrl The WebSocket server URL
     */
    void connect(String serverUrl);
    
    /**
     * Disconnect from WebSocket server.
     */
    void disconnect();
    
    /**
     * Get current connection state.
     * @return Current connection state
     */
    ConnectionState getConnectionState();
    
    /**
     * Check if currently connected to server.
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Send message to server.
     * @param message Message to send
     */
    void sendMessage(NetworkMessage message);
    
    /**
     * Handle incoming message from server.
     * @param rawMessage Raw message string
     */
    void handleIncomingMessage(String rawMessage);
    
    /**
     * Shutdown the WebSocket service and cleanup resources.
     */
    void shutdown();
}