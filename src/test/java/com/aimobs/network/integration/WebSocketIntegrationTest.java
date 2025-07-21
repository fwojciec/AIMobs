package com.aimobs.network.integration;

import com.aimobs.network.WebSocketService;
import com.aimobs.network.application.NetworkCommandCoordinator;
import com.aimobs.network.application.TestableWebSocketService;
import com.aimobs.network.core.ConnectionState;
import com.aimobs.network.core.NetworkMessage;
import com.aimobs.test.BaseUnitTest;
import com.aimobs.test.FakeMessageService;
import com.aimobs.test.FakeWebSocketConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests using ONLY fake objects - no mocking framework.
 * Following Feathers' principle: "Fake objects are often better than mocks"
 * Fast, deterministic, no external dependencies.
 */
@Tag("unit") 
class WebSocketIntegrationTest extends BaseUnitTest {
    
    private WebSocketService webSocketService;
    private FakeWebSocketConnection fakeConnection;
    private FakeMessageService fakeMessageService;
    private NetworkCommandCoordinator coordinator;
    
    @BeforeEach
    void setUp() {
        fakeMessageService = new FakeMessageService();
        fakeConnection = new FakeWebSocketConnection();
        coordinator = new NetworkCommandCoordinator(fakeMessageService);
        webSocketService = new TestableWebSocketService(fakeConnection, coordinator);
    }
    
    @Test
    void shouldHandleCompleteMessageFlow() {
        // Given: connected service
        webSocketService.connect("ws://test-server:8080");
        assertTrue(webSocketService.isConnected());
        assertEquals(ConnectionState.CONNECTED, webSocketService.getConnectionState());
        
        // When: receive valid message
        String incomingMessage = """
            {
              "type": "command",
              "timestamp": "2025-01-19T10:00:00Z",
              "data": {
                "action": "move",
                "parameters": {"x": 10},
                "context": {}
              }
            }
            """;
        
        fakeConnection.simulateIncomingMessage(incomingMessage);
        
        // Then: message should be processed and command queued
        assertEquals(1, fakeMessageService.getQueuedCommandCount());
    }
    
    @Test
    void shouldHandleConnectionFailures() {
        // Given: connection that will fail
        fakeConnection.setConnectionFailure(true);
        
        // When: attempt to connect
        webSocketService.connect("ws://test-server:8080");
        
        // Then: should handle failure gracefully
        assertFalse(webSocketService.isConnected());
        assertEquals(ConnectionState.ERROR, webSocketService.getConnectionState());
    }
    
    @Test
    void shouldSendOutgoingMessages() {
        // Given: connected service
        webSocketService.connect("ws://test-server:8080");
        
        // When: send message
        NetworkMessage.MessageData data = new NetworkMessage.MessageData(
            "move", Map.of("x", 10), Map.of()
        );
        NetworkMessage message = new NetworkMessage("command", "2025-01-19T10:00:00Z", data);
        
        webSocketService.sendMessage(message);
        
        // Then: message should be sent through connection
        assertEquals(1, fakeConnection.getSentMessages().size());
        String sentMessage = fakeConnection.getSentMessages().get(0);
        assertTrue(sentMessage.contains("\"action\": \"move\""));
    }
    
    @Test
    void shouldNotSendInvalidMessages() {
        // Given: connected service that validates messages as invalid
        webSocketService.connect("ws://test-server:8080");
        fakeMessageService.setShouldValidate(false);
        
        // When: try to send invalid message
        NetworkMessage.MessageData data = new NetworkMessage.MessageData(
            "invalid", Map.of(), Map.of()
        );
        NetworkMessage message = new NetworkMessage("command", "2025-01-19T10:00:00Z", data);
        
        webSocketService.sendMessage(message);
        
        // Then: no message should be sent
        assertEquals(0, fakeConnection.getSentMessages().size());
    }
    
    @Test
    void shouldHandleReconnectionFlow() {
        // Given: connected service
        webSocketService.connect("ws://test-server:8080");
        assertTrue(webSocketService.isConnected());
        
        // When: connection is lost
        fakeConnection.simulateDisconnection();
        
        // Then: should detect disconnection and trigger reconnection attempt
        assertFalse(webSocketService.isConnected());
        assertEquals(ConnectionState.RECONNECTING, webSocketService.getConnectionState());
        
        // When: reconnect
        fakeConnection.reset();  // Reset for fresh connection
        webSocketService.connect("ws://test-server:8080");
        
        // Then: should be connected again
        assertTrue(webSocketService.isConnected());
        assertEquals(ConnectionState.CONNECTED, webSocketService.getConnectionState());
    }
    
    @Test
    void shouldRejectMalformedMessages() {
        // Given: connected service with message parsing configured to fail
        webSocketService.connect("ws://test-server:8080");
        fakeMessageService.setShouldParseSuccessfully(false);
        
        // When: receive malformed message
        fakeConnection.simulateIncomingMessage("{ invalid json }");
        
        // Then: no commands should be queued
        assertEquals(0, fakeMessageService.getQueuedCommandCount());
    }
    
    @Test
    void shouldCleanupProperly() {
        // Given: connected service
        webSocketService.connect("ws://test-server:8080");
        assertTrue(webSocketService.isConnected());
        
        // When: shutdown
        webSocketService.shutdown();
        
        // Then: should be disconnected
        assertFalse(webSocketService.isConnected());
    }
}