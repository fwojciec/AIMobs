package com.aimobs.network.infrastructure;

import com.aimobs.network.core.WebSocketConnection;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * Production implementation using Java-WebSocket library.
 * This is the "how" - the concrete network implementation.
 */
public class JavaWebSocketConnection implements WebSocketConnection {
    
    private WebSocketClient client;
    private ConnectionListener listener;
    
    @Override
    public void connect(String serverUrl, ConnectionListener listener) {
        this.listener = listener;
        
        try {
            URI serverUri = new URI(serverUrl);
            
            client = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    if (JavaWebSocketConnection.this.listener != null) {
                        JavaWebSocketConnection.this.listener.onConnected();
                    }
                }
                
                @Override
                public void onMessage(String message) {
                    if (JavaWebSocketConnection.this.listener != null) {
                        JavaWebSocketConnection.this.listener.onMessageReceived(message);
                    }
                }
                
                @Override
                public void onClose(int code, String reason, boolean remote) {
                    if (JavaWebSocketConnection.this.listener != null) {
                        JavaWebSocketConnection.this.listener.onDisconnected();
                    }
                }
                
                @Override
                public void onError(Exception ex) {
                    if (JavaWebSocketConnection.this.listener != null) {
                        JavaWebSocketConnection.this.listener.onError(ex);
                    }
                }
            };
            
            // Connect with timeout to prevent hanging
            boolean connected = client.connectBlocking(5, TimeUnit.SECONDS);
            if (!connected) {
                if (listener != null) {
                    listener.onError(new RuntimeException("WebSocket connection timeout after 5 seconds"));
                }
            }
            
        } catch (URISyntaxException e) {
            if (listener != null) {
                listener.onError(new IllegalArgumentException("Invalid WebSocket server URL: " + serverUrl, e));
            }
        } catch (InterruptedException e) {
            if (listener != null) {
                listener.onError(new RuntimeException("WebSocket connection interrupted", e));
            }
        } catch (IllegalArgumentException e) {
            if (listener != null) {
                listener.onError(e);
            }
        } catch (RuntimeException e) {
            if (listener != null) {
                listener.onError(new RuntimeException("Failed to establish WebSocket connection", e));
            }
        }
    }
    
    @Override
    public void disconnect() {
        if (client != null) {
            client.close();
        }
    }
    
    @Override
    public boolean isConnected() {
        return client != null && client.isOpen();
    }
    
    @Override
    public void sendMessage(String message) {
        if (client != null && client.isOpen()) {
            client.send(message);
        }
    }
    
    @Override
    public void cleanup() {
        if (client != null) {
            client.close();
            client = null;
        }
        listener = null;
    }
}