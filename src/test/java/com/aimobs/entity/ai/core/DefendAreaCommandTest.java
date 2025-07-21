package com.aimobs.entity.ai.core;

import com.aimobs.test.BaseUnitTest;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DefendAreaCommand following TDD approach.
 * Tests pure domain logic in isolation.
 */
class DefendAreaCommandTest extends BaseUnitTest {
    
    @Test
    void shouldCreateWithBasicParameters() {
        BlockPos centerPos = new BlockPos(10, 64, 20);
        
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 15.0);
        
        assertEquals(centerPos, command.getCenterPos());
        assertEquals(15.0, command.getRadius());
        assertEquals(InteractionType.DEFEND, command.getInteractionType());
        assertEquals(InteractionType.DEFEND.getDefaultPriority(), command.getPriority());
        assertTrue(command.requiresPositioning());
        assertFalse(command.isComplete());
        assertFalse(command.isCancelled());
    }
    
    @Test
    void shouldCreateWithCustomPriority() {
        BlockPos centerPos = new BlockPos(0, 64, 0);
        
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 20.0, 9);
        
        assertEquals(9, command.getPriority());
    }
    
    @Test
    void shouldNotCompleteByDefault() {
        BlockPos centerPos = new BlockPos(5, 64, 5);
        
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 10.0);
        
        // Defense commands should continue indefinitely until cancelled
        assertFalse(command.isComplete());
        
        command.execute();
        assertFalse(command.isComplete());
    }
    
    @Test
    void shouldCancelCorrectly() {
        BlockPos centerPos = new BlockPos(0, 64, 0);
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 15.0);
        
        assertFalse(command.isCancelled());
        assertFalse(command.isComplete());
        
        command.cancel();
        
        assertTrue(command.isCancelled());
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldExecuteWithoutError() {
        BlockPos centerPos = new BlockPos(0, 64, 0);
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 15.0);
        
        assertDoesNotThrow(() -> command.execute());
    }
    
    @Test
    void shouldBeCompleteWhenCancelled() {
        BlockPos centerPos = new BlockPos(0, 64, 0);
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 15.0);
        
        command.cancel();
        command.execute();
        
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldHandleNegativeCoordinates() {
        BlockPos centerPos = new BlockPos(-10, 64, -20);
        
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 25.0);
        
        assertEquals(centerPos, command.getCenterPos());
        assertEquals(25.0, command.getRadius());
    }
    
    @Test
    void shouldHandleLargeRadius() {
        BlockPos centerPos = new BlockPos(0, 64, 0);
        
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 1000.0);
        
        assertEquals(1000.0, command.getRadius());
    }
    
    @Test
    void shouldHandleSmallRadius() {
        BlockPos centerPos = new BlockPos(0, 64, 0);
        
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 0.5);
        
        assertEquals(0.5, command.getRadius());
    }
    
    @Test
    void shouldMaintainStateConsistency() {
        BlockPos centerPos = new BlockPos(100, 64, 200);
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 30.0, 6);
        
        // Initial state
        assertEquals(centerPos, command.getCenterPos());
        assertEquals(30.0, command.getRadius());
        assertEquals(6, command.getPriority());
        assertEquals(InteractionType.DEFEND, command.getInteractionType());
        assertTrue(command.requiresPositioning());
        assertFalse(command.isComplete());
        assertFalse(command.isCancelled());
        
        // After execution (should remain incomplete)
        command.execute();
        assertFalse(command.isComplete());
        assertFalse(command.isCancelled());
        
        // After cancellation
        command.cancel();
        assertTrue(command.isComplete());
        assertTrue(command.isCancelled());
    }
    
    @Test
    void shouldHandleZeroRadius() {
        BlockPos centerPos = new BlockPos(0, 64, 0);
        
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 0.0);
        
        assertEquals(0.0, command.getRadius());
        // Command should still be valid even with zero radius
        assertFalse(command.isComplete());
    }
    
    @Test
    void shouldHandleHighYCoordinates() {
        BlockPos centerPos = new BlockPos(0, 320, 0); // High Y coordinate
        
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 15.0);
        
        assertEquals(centerPos, command.getCenterPos());
        assertEquals(320, command.getCenterPos().getY());
    }
    
    @Test
    void shouldHandleLowYCoordinates() {
        BlockPos centerPos = new BlockPos(0, -64, 0); // Low Y coordinate
        
        DefendAreaCommand command = new DefendAreaCommand(centerPos, 15.0);
        
        assertEquals(centerPos, command.getCenterPos());
        assertEquals(-64, command.getCenterPos().getY());
    }
}