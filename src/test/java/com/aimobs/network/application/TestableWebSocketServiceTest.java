package com.aimobs.network.application;

import com.aimobs.network.core.ConnectionState;
import com.aimobs.network.core.NetworkMessage;
import com.aimobs.test.BaseUnitTest;
import com.aimobs.test.FakeWebSocketConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.mockito.Mock;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
class TestableWebSocketServiceTest extends BaseUnitTest {
    
    private TestableWebSocketService webSocketService;
    private FakeWebSocketConnection fakeConnection;
    
    @Mock
    private NetworkCommandCoordinator mockCoordinator;
    
    @BeforeEach
    void setUp() {
        mockCoordinator = mock(NetworkCommandCoordinator.class);
        fakeConnection = new FakeWebSocketConnection();
        webSocketService = new TestableWebSocketService(fakeConnection, mockCoordinator);
    }
    
    @Test
    void shouldConnectUsingConnection() {
        String serverUrl = "ws://test-server:8080";
        
        webSocketService.connect(serverUrl);
        
        assertTrue(fakeConnection.isConnected());
        assertTrue(webSocketService.isConnected());
    }
    
    @Test
    void shouldDisconnectProperly() {
        webSocketService.connect("ws://test-server:8080");
        assertTrue(webSocketService.isConnected());
        
        webSocketService.disconnect();
        
        assertFalse(fakeConnection.isConnected());
        assertFalse(webSocketService.isConnected());
    }
    
    @Test
    void shouldHandleConnectionFailures() {
        fakeConnection.setConnectionFailure(true);
        
        webSocketService.connect("ws://test-server:8080");
        
        assertFalse(webSocketService.isConnected());
        // Connection failure should be reported to coordinator
        verify(mockCoordinator).onError(any(Exception.class));
    }
    
    @Test
    void shouldSendValidMessages() {
        NetworkMessage message = createValidMessage();
        String expectedJson = "test-json";
        
        when(mockCoordinator.prepareOutgoingMessage(message)).thenReturn(expectedJson);
        
        webSocketService.connect("ws://test-server:8080");
        webSocketService.sendMessage(message);
        
        assertEquals(1, fakeConnection.getSentMessages().size());
        assertEquals(expectedJson, fakeConnection.getSentMessages().get(0));
    }
    
    @Test
    void shouldNotSendWhenDisconnected() {
        NetworkMessage message = createValidMessage();
        
        webSocketService.sendMessage(message);
        
        assertEquals(0, fakeConnection.getSentMessages().size());
        verify(mockCoordinator, never()).prepareOutgoingMessage(any());
    }
    
    @Test
    void shouldNotSendInvalidMessages() {
        NetworkMessage message = createValidMessage();
        
        when(mockCoordinator.prepareOutgoingMessage(message)).thenReturn(null);
        
        webSocketService.connect("ws://test-server:8080");
        webSocketService.sendMessage(message);
        
        assertEquals(0, fakeConnection.getSentMessages().size());
    }
    
    @Test
    void shouldDelegateIncomingMessages() {
        String incomingMessage = "test-message";
        
        webSocketService.handleIncomingMessage(incomingMessage);
        
        verify(mockCoordinator).handleIncomingMessage(incomingMessage);
    }
    
    @Test
    void shouldDelegateConnectionState() {
        when(mockCoordinator.getConnectionState()).thenReturn(ConnectionState.CONNECTED);
        
        assertEquals(ConnectionState.CONNECTED, webSocketService.getConnectionState());
        
        verify(mockCoordinator).getConnectionState();
    }
    
    @Test
    void shouldCleanupOnShutdown() {
        webSocketService.connect("ws://test-server:8080");
        assertTrue(webSocketService.isConnected());
        
        webSocketService.shutdown();
        
        assertFalse(webSocketService.isConnected());
    }
    
    @Test
    void shouldReceiveMessagesFromConnection() {
        String incomingMessage = "incoming-test";
        
        webSocketService.connect("ws://test-server:8080");
        fakeConnection.simulateIncomingMessage(incomingMessage);
        
        verify(mockCoordinator).onMessageReceived(incomingMessage);
    }
    
    @Test
    void shouldHandleConnectionEvents() {
        webSocketService.connect("ws://test-server:8080");
        
        verify(mockCoordinator).onConnected();
        
        fakeConnection.simulateDisconnection();
        verify(mockCoordinator).onDisconnected();
        
        Exception testError = new RuntimeException("Test error");
        fakeConnection.simulateError(testError);
        verify(mockCoordinator).onError(testError);
    }
    
    private NetworkMessage createValidMessage() {
        NetworkMessage.MessageData data = new NetworkMessage.MessageData(
            "move", Map.of("x", 10), Map.of()
        );
        return new NetworkMessage("command", "2025-01-19T10:00:00Z", data);
    }
}