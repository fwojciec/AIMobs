package com.aimobs.network.application;

import com.aimobs.entity.ai.CommandProcessorService;
import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.network.MessageService;
import com.aimobs.network.core.NetworkMessage;
import com.aimobs.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@Tag("unit")
class MessageParserTest extends BaseUnitTest {
    
    private MessageService messageService;
    
    @Mock
    private CommandProcessorService mockCommandProcessor;
    
    @BeforeEach
    void setUp() {
        mockCommandProcessor = mock(CommandProcessorService.class);
        // Use legacy constructor with null command router for backward compatibility
        messageService = new MessageParser(mockCommandProcessor, null);
    }
    
    @Test
    void shouldParseValidJsonMessage() {
        String validJson = """
            {
              "type": "command",
              "timestamp": "2025-01-19T10:00:00Z",
              "data": {
                "action": "move",
                "parameters": {"x": 10, "y": 64, "z": 10},
                "context": {}
              }
            }
            """;
        
        NetworkMessage message = messageService.parseMessage(validJson);
        
        assertNotNull(message);
        assertEquals("command", message.getType());
        assertEquals("move", message.getData().getAction());
        assertEquals(10, ((Double) message.getData().getParameters().get("x")).intValue());
    }
    
    @Test
    void shouldReturnNullForMalformedJson() {
        String invalidJson = "{ invalid json }";
        
        NetworkMessage message = messageService.parseMessage(invalidJson);
        
        assertNull(message);
    }
    
    @Test
    void shouldReturnNullForNullInput() {
        NetworkMessage message = messageService.parseMessage(null);
        
        assertNull(message);
    }
    
    @Test
    void shouldReturnNullForEmptyInput() {
        NetworkMessage message = messageService.parseMessage("");
        
        assertNull(message);
    }
    
    @Test
    void shouldValidateValidMessage() {
        NetworkMessage validMessage = createValidMessage();
        
        assertTrue(messageService.validateMessage(validMessage));
    }
    
    @Test
    void shouldRejectNullMessage() {
        assertFalse(messageService.validateMessage(null));
    }
    
    @Test
    void shouldRejectInvalidMessage() {
        NetworkMessage invalidMessage = new NetworkMessage(null, null, null);
        
        assertFalse(messageService.validateMessage(invalidMessage));
    }
    
    @Test
    void shouldConvertValidMessageToCommand() {
        NetworkMessage validMessage = createValidMessage();
        
        AICommand command = messageService.convertToCommand(validMessage);
        
        assertNotNull(command);
        assertFalse(command.isComplete());
    }
    
    @Test
    void shouldReturnNullCommandForInvalidMessage() {
        NetworkMessage invalidMessage = new NetworkMessage(null, null, null);
        
        AICommand command = messageService.convertToCommand(invalidMessage);
        
        assertNull(command);
    }
    
    @Test
    void shouldQueueValidCommands() {
        AICommand command = mock(AICommand.class);
        
        messageService.queueCommand(command);
        
        assertEquals(1, messageService.getQueuedCommandCount());
    }
    
    @Test
    void shouldIgnoreNullCommands() {
        messageService.queueCommand(null);
        
        assertEquals(0, messageService.getQueuedCommandCount());
    }
    
    @Test
    void shouldClearQueue() {
        AICommand command = mock(AICommand.class);
        messageService.queueCommand(command);
        
        assertEquals(1, messageService.getQueuedCommandCount());
        
        messageService.clearQueue();
        
        assertEquals(0, messageService.getQueuedCommandCount());
    }
    
    private NetworkMessage createValidMessage() {
        NetworkMessage.MessageData data = new NetworkMessage.MessageData(
            "move",
            java.util.Map.of("x", 10, "y", 64, "z", 10),
            java.util.Map.of()
        );
        return new NetworkMessage("command", "2025-01-19T10:00:00Z", data);
    }
}