package com.aimobs.network.core;

import com.aimobs.test.BaseUnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class NetworkMessageTest extends BaseUnitTest {
    
    @Test
    void shouldValidateCompleteMessage() {
        NetworkMessage message = createValidMessage();
        
        assertTrue(message.isValid());
    }
    
    @Test
    void shouldRejectMessageWithoutType() {
        NetworkMessage message = new NetworkMessage(null, "2025-01-19T10:00:00Z", createValidData());
        
        assertFalse(message.isValid());
    }
    
    @Test
    void shouldRejectMessageWithEmptyType() {
        NetworkMessage message = new NetworkMessage("", "2025-01-19T10:00:00Z", createValidData());
        
        assertFalse(message.isValid());
    }
    
    @Test
    void shouldRejectMessageWithoutTimestamp() {
        NetworkMessage message = new NetworkMessage("command", null, createValidData());
        
        assertFalse(message.isValid());
    }
    
    @Test
    void shouldRejectMessageWithEmptyTimestamp() {
        NetworkMessage message = new NetworkMessage("command", "", createValidData());
        
        assertFalse(message.isValid());
    }
    
    @Test
    void shouldRejectMessageWithoutData() {
        NetworkMessage message = new NetworkMessage("command", "2025-01-19T10:00:00Z", null);
        
        assertFalse(message.isValid());
    }
    
    @Test
    void shouldRejectMessageWithInvalidData() {
        NetworkMessage.MessageData invalidData = new NetworkMessage.MessageData(
            "invalid_action", Map.of(), Map.of()
        );
        NetworkMessage message = new NetworkMessage("command", "2025-01-19T10:00:00Z", invalidData);
        
        assertFalse(message.isValid());
    }
    
    @Test
    void shouldAcceptValidActionTypes() {
        String[] validActions = {"move", "attack", "speak", "status"};
        
        for (String action : validActions) {
            NetworkMessage.MessageData data = new NetworkMessage.MessageData(
                action, Map.of("x", 10), Map.of()
            );
            NetworkMessage message = new NetworkMessage("command", "2025-01-19T10:00:00Z", data);
            
            assertTrue(message.isValid(), "Action '" + action + "' should be valid");
        }
    }
    
    @Test
    void shouldProvideAccessToFields() {
        NetworkMessage message = createValidMessage();
        
        assertEquals("command", message.getType());
        assertEquals("2025-01-19T10:00:00Z", message.getTimestamp());
        assertNotNull(message.getData());
        assertEquals("move", message.getData().getAction());
    }
    
    @Test
    void shouldProvideAccessToDataFields() {
        NetworkMessage.MessageData data = createValidData();
        
        assertEquals("move", data.getAction());
        assertNotNull(data.getParameters());
        assertNotNull(data.getContext());
        assertEquals(10, data.getParameters().get("x"));
    }
    
    private NetworkMessage createValidMessage() {
        return new NetworkMessage("command", "2025-01-19T10:00:00Z", createValidData());
    }
    
    private NetworkMessage.MessageData createValidData() {
        return new NetworkMessage.MessageData(
            "move", 
            Map.of("x", 10, "y", 64, "z", 10), 
            Map.of()
        );
    }
}