package com.aimobs.network.core;

/**
 * Core abstraction for WebSocket connections - this is our SEAM.
 * Following Feathers' approach: we can substitute different implementations
 * without changing the code that uses this interface.
 */
public interface WebSocketConnection {
    
    /**
     * Connection state change listener.
     */
    interface ConnectionListener {
        void onConnected();
        void onDisconnected();
        void onError(Exception error);
        void onMessageReceived(String message);
    }
    
    /**
     * Establish connection to server.
     * @param serverUrl The WebSocket server URL
     * @param listener Connection event listener
     */
    void connect(String serverUrl, ConnectionListener listener);
    
    /**
     * Disconnect from server.
     */
    void disconnect();
    
    /**
     * Check if currently connected.
     * @return true if connected, false otherwise
     */
    boolean isConnected();
    
    /**
     * Send message to server.
     * @param message Message to send
     */
    void sendMessage(String message);
    
    /**
     * Cleanup resources.
     */
    void cleanup();
}