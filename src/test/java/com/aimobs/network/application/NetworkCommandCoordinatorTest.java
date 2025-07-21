package com.aimobs.network.application;

import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.network.MessageService;
import com.aimobs.network.core.ConnectionState;
import com.aimobs.network.core.NetworkMessage;
import com.aimobs.test.BaseUnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.mockito.Mock;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Tag("unit")
class NetworkCommandCoordinatorTest extends BaseUnitTest {
    
    private NetworkCommandCoordinator coordinator;
    
    @Mock
    private MessageService mockMessageService;
    
    @BeforeEach
    void setUp() {
        mockMessageService = mock(MessageService.class);
        coordinator = new NetworkCommandCoordinator(mockMessageService);
    }
    
    @AfterEach
    void tearDown() {
        // Clean up the executor to prevent resource leaks
        coordinator.shutdown();
    }
    
    @Test
    void shouldStartInDisconnectedState() {
        assertEquals(ConnectionState.DISCONNECTED, coordinator.getConnectionState());
    }
    
    @Test
    void shouldHandleValidIncomingMessage() {
        String validJson = """
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
        
        NetworkMessage message = createValidMessage();
        AICommand command = mock(AICommand.class);
        
        when(mockMessageService.parseMessage(validJson)).thenReturn(message);
        when(mockMessageService.validateMessage(message)).thenReturn(true);
        when(mockMessageService.convertToCommand(message)).thenReturn(command);
        
        coordinator.handleIncomingMessage(validJson);
        
        verify(mockMessageService).parseMessage(validJson);
        verify(mockMessageService).validateMessage(message);
        verify(mockMessageService).convertToCommand(message);
        verify(mockMessageService).queueCommand(command);
    }
    
    @Test
    void shouldRejectInvalidMessages() {
        String invalidJson = "invalid";
        
        when(mockMessageService.parseMessage(invalidJson)).thenReturn(null);
        
        coordinator.handleIncomingMessage(invalidJson);
        
        verify(mockMessageService).parseMessage(invalidJson);
        verify(mockMessageService, never()).validateMessage(any());
        verify(mockMessageService, never()).queueCommand(any());
    }
    
    @Test
    void shouldIgnoreNullMessages() {
        coordinator.handleIncomingMessage(null);
        
        verify(mockMessageService, never()).parseMessage(any());
    }
    
    @Test
    void shouldIgnoreEmptyMessages() {
        coordinator.handleIncomingMessage("");
        
        verify(mockMessageService, never()).parseMessage(any());
    }
    
    @Test
    void shouldPrepareValidOutgoingMessage() {
        NetworkMessage message = createValidMessage();
        when(mockMessageService.validateMessage(message)).thenReturn(true);
        
        String result = coordinator.prepareOutgoingMessage(message);
        
        assertNotNull(result);
        assertTrue(result.contains("\"type\": \"command\""));
        assertTrue(result.contains("\"action\": \"move\""));
    }
    
    @Test
    void shouldRejectInvalidOutgoingMessage() {
        NetworkMessage invalidMessage = createValidMessage();
        when(mockMessageService.validateMessage(invalidMessage)).thenReturn(false);
        
        String result = coordinator.prepareOutgoingMessage(invalidMessage);
        
        assertNull(result);
    }
    
    @Test
    void shouldTrackConnectionStateChanges() {
        coordinator.onConnected();
        assertEquals(ConnectionState.CONNECTED, coordinator.getConnectionState());
        assertNull(coordinator.getLastError());
        
        // Disconnection now triggers reconnection attempt
        coordinator.onDisconnected();
        assertEquals(ConnectionState.RECONNECTING, coordinator.getConnectionState());
        
        // Test error handling for non-recoverable errors
        Exception nonRecoverableError = new RuntimeException("Authentication failed");
        coordinator.onError(nonRecoverableError);
        assertEquals(ConnectionState.ERROR, coordinator.getConnectionState());
        assertEquals("Authentication failed", coordinator.getLastError());
    }
    
    @Test
    void shouldDelegateQueueCountToMessageService() {
        when(mockMessageService.getQueuedCommandCount()).thenReturn(5);
        
        assertEquals(5, coordinator.getQueuedCommandCount());
        
        verify(mockMessageService).getQueuedCommandCount();
    }
    
    private NetworkMessage createValidMessage() {
        NetworkMessage.MessageData data = new NetworkMessage.MessageData(
            "move", Map.of("x", 10), Map.of()
        );
        return new NetworkMessage("command", "2025-01-19T10:00:00Z", data);
    }
}