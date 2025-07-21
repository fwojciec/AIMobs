package com.aimobs.entity.ai.core;

import com.aimobs.test.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CommunicationCommand following TDD approach.
 * Tests pure domain logic in isolation.
 */
class CommunicationCommandTest extends BaseUnitTest {
    
    @Test
    void shouldCreateWithMessage() {
        CommunicationCommand command = new CommunicationCommand("Hello, how are you?");
        
        assertEquals("Hello, how are you?", command.getMessage());
        assertEquals(InteractionType.COMMUNICATE, command.getInteractionType());
        assertEquals(InteractionType.COMMUNICATE.getDefaultPriority(), command.getPriority());
        assertFalse(command.requiresPositioning());
        assertFalse(command.isComplete());
        assertFalse(command.isCancelled());
        assertNull(command.getResponse());
    }
    
    @Test
    void shouldCreateWithCustomPriority() {
        CommunicationCommand command = new CommunicationCommand("Status report", 8);
        
        assertEquals(8, command.getPriority());
    }
    
    @Test
    void shouldCreateWithNullMessage() {
        CommunicationCommand command = new CommunicationCommand(null);
        
        assertNull(command.getMessage());
        assertEquals(InteractionType.COMMUNICATE, command.getInteractionType());
    }
    
    @Test
    void shouldCreateWithEmptyMessage() {
        CommunicationCommand command = new CommunicationCommand("");
        
        assertEquals("", command.getMessage());
    }
    
    @Test
    void shouldCompleteAfterExecution() {
        CommunicationCommand command = new CommunicationCommand("Test message");
        
        assertFalse(command.isComplete());
        
        command.execute();
        
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldSetAndGetResponse() {
        CommunicationCommand command = new CommunicationCommand("What's your status?");
        
        assertNull(command.getResponse());
        
        command.setResponse("I'm doing well, thanks!");
        
        assertEquals("I'm doing well, thanks!", command.getResponse());
    }
    
    @Test
    void shouldCancelCorrectly() {
        CommunicationCommand command = new CommunicationCommand("Hello");
        
        assertFalse(command.isCancelled());
        assertFalse(command.isComplete());
        
        command.cancel();
        
        assertTrue(command.isCancelled());
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldBeCompleteWhenCancelled() {
        CommunicationCommand command = new CommunicationCommand("Test");
        
        command.cancel();
        command.execute();
        
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldNotRequirePositioning() {
        CommunicationCommand command = new CommunicationCommand("Any message");
        
        assertFalse(command.requiresPositioning());
    }
    
    @Test
    void shouldExecuteWithoutError() {
        CommunicationCommand command = new CommunicationCommand("Test message");
        
        assertDoesNotThrow(() -> command.execute());
    }
    
    @Test
    void shouldHandleLongMessage() {
        String longMessage = "This is a very long message that contains many words and should be handled properly by the communication command without any issues or errors even though it exceeds normal message lengths.";
        
        CommunicationCommand command = new CommunicationCommand(longMessage);
        
        assertEquals(longMessage, command.getMessage());
        command.execute();
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldHandleSpecialCharacters() {
        String messageWithSpecialChars = "Hello! @#$%^&*()_+ 123 ñáéíóú 中文 🎮🤖";
        
        CommunicationCommand command = new CommunicationCommand(messageWithSpecialChars);
        
        assertEquals(messageWithSpecialChars, command.getMessage());
    }
    
    @Test
    void shouldAllowResponseOverwrite() {
        CommunicationCommand command = new CommunicationCommand("Question?");
        
        command.setResponse("First response");
        assertEquals("First response", command.getResponse());
        
        command.setResponse("Updated response");
        assertEquals("Updated response", command.getResponse());
    }
    
    @Test
    void shouldAllowNullResponse() {
        CommunicationCommand command = new CommunicationCommand("Question?");
        
        command.setResponse("Some response");
        assertNotNull(command.getResponse());
        
        command.setResponse(null);
        assertNull(command.getResponse());
    }
    
    @Test
    void shouldMaintainStateConsistency() {
        CommunicationCommand command = new CommunicationCommand("How are you?", 3);
        
        // Initial state
        assertEquals("How are you?", command.getMessage());
        assertEquals(3, command.getPriority());
        assertEquals(InteractionType.COMMUNICATE, command.getInteractionType());
        assertFalse(command.requiresPositioning());
        assertFalse(command.isComplete());
        assertFalse(command.isCancelled());
        assertNull(command.getResponse());
        
        // After setting response
        command.setResponse("I'm fine, thank you!");
        assertEquals("I'm fine, thank you!", command.getResponse());
        assertFalse(command.isComplete()); // Should not complete just from setting response
        
        // After execution
        command.execute();
        assertTrue(command.isComplete());
        assertFalse(command.isCancelled());
        assertEquals("I'm fine, thank you!", command.getResponse()); // Response should persist
        
        // Original message should remain unchanged
        assertEquals("How are you?", command.getMessage());
    }
    
    @Test
    void shouldCompleteImmediatelyOnExecute() {
        CommunicationCommand command = new CommunicationCommand("Quick message");
        
        // Communication commands should complete immediately after execution
        // since they don't require ongoing actions like movement or combat
        assertFalse(command.isComplete());
        
        command.execute();
        
        assertTrue(command.isComplete());
    }
}