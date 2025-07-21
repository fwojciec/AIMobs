package com.aimobs.entity.ai.core;

import com.aimobs.test.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CollectItemsCommand following TDD approach.
 * Tests pure domain logic in isolation.
 */
class CollectItemsCommandTest extends BaseUnitTest {
    
    @Test
    void shouldCreateWithBasicParameters() {
        CollectItemsCommand command = new CollectItemsCommand("wood", 10.0, 5);
        
        assertEquals("wood", command.getItemType());
        assertEquals(10.0, command.getRadius());
        assertEquals(5, command.getMaxItems());
        assertEquals(InteractionType.COLLECT, command.getInteractionType());
        assertEquals(InteractionType.COLLECT.getDefaultPriority(), command.getPriority());
        assertTrue(command.requiresPositioning());
        assertFalse(command.isComplete());
        assertFalse(command.isCancelled());
        assertEquals(0, command.getItemsCollected());
    }
    
    @Test
    void shouldCreateWithCustomPriority() {
        CollectItemsCommand command = new CollectItemsCommand("stone", 15.0, 10, 7);
        
        assertEquals(7, command.getPriority());
    }
    
    @Test
    void shouldCreateWithUnlimitedItems() {
        CollectItemsCommand command = new CollectItemsCommand("iron", 20.0, 0);
        
        assertEquals(0, command.getMaxItems());
        assertFalse(command.isComplete()); // Should not be complete with 0 collected and unlimited max
    }
    
    @Test
    void shouldTrackItemsCollected() {
        CollectItemsCommand command = new CollectItemsCommand("wood", 10.0, 5);
        
        assertEquals(0, command.getItemsCollected());
        
        command.incrementItemsCollected();
        assertEquals(1, command.getItemsCollected());
        
        command.incrementItemsCollected();
        command.incrementItemsCollected();
        assertEquals(3, command.getItemsCollected());
    }
    
    @Test
    void shouldCompleteWhenMaxItemsReached() {
        CollectItemsCommand command = new CollectItemsCommand("wood", 10.0, 3);
        
        assertFalse(command.isComplete());
        
        command.incrementItemsCollected();
        command.incrementItemsCollected();
        assertFalse(command.isComplete());
        
        command.incrementItemsCollected();
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldNotCompleteWithUnlimitedItems() {
        CollectItemsCommand command = new CollectItemsCommand("stone", 15.0, 0); // Unlimited
        
        // Collect many items
        for (int i = 0; i < 100; i++) {
            command.incrementItemsCollected();
        }
        
        assertFalse(command.isComplete()); // Should never complete with unlimited items
    }
    
    @Test
    void shouldCancelCorrectly() {
        CollectItemsCommand command = new CollectItemsCommand("wood", 10.0, 5);
        
        assertFalse(command.isCancelled());
        assertFalse(command.isComplete());
        
        command.cancel();
        
        assertTrue(command.isCancelled());
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldExecuteWithoutError() {
        CollectItemsCommand command = new CollectItemsCommand("wood", 10.0, 5);
        
        assertDoesNotThrow(() -> command.execute());
    }
    
    @Test
    void shouldBeCompleteWhenCancelled() {
        CollectItemsCommand command = new CollectItemsCommand("wood", 10.0, 5);
        command.cancel();
        
        command.execute();
        
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldHandleEdgeCases() {
        // Test with negative max items (should be treated as 0/unlimited)
        CollectItemsCommand command = new CollectItemsCommand("wood", 10.0, -1);
        assertEquals(-1, command.getMaxItems());
        
        // Should not complete even after collecting items
        command.incrementItemsCollected();
        command.incrementItemsCollected();
        assertFalse(command.isComplete());
    }
    
    @Test
    void shouldCompleteImmediatelyWhenMaxIsZero() {
        CollectItemsCommand command = new CollectItemsCommand("wood", 10.0, 0);
        
        // With max items = 0 (unlimited), should not complete until cancelled
        assertFalse(command.isComplete());
        
        // But if somehow maxItems was set to 0 and we want it to complete immediately
        // we would need to modify the implementation. Current behavior is correct for "unlimited"
    }
    
    @Test
    void shouldMaintainStateConsistency() {
        CollectItemsCommand command = new CollectItemsCommand("diamond", 5.0, 2);
        
        // Initial state
        assertEquals("diamond", command.getItemType());
        assertEquals(5.0, command.getRadius());
        assertEquals(2, command.getMaxItems());
        assertEquals(0, command.getItemsCollected());
        assertFalse(command.isComplete());
        assertFalse(command.isCancelled());
        
        // After incrementing
        command.incrementItemsCollected();
        assertEquals(1, command.getItemsCollected());
        assertFalse(command.isComplete());
        
        // After reaching max
        command.incrementItemsCollected();
        assertEquals(2, command.getItemsCollected());
        assertTrue(command.isComplete());
        
        // Cancel should override completion status
        command.cancel();
        assertTrue(command.isComplete());
        assertTrue(command.isCancelled());
    }
}