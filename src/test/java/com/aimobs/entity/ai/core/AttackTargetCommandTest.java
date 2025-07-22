package com.aimobs.entity.ai.core;

import com.aimobs.test.BaseUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AttackTargetCommand following TDD approach.
 * Tests pure domain logic in isolation.
 */
class AttackTargetCommandTest extends BaseUnitTest {
    
    @Test
    void shouldCreateWithTarget() {
        TargetEntity fakeTarget = new FakeTargetEntity("zombie");
        
        AttackTargetCommand command = new AttackTargetCommand(fakeTarget);
        
        assertEquals(fakeTarget, command.getTarget());
        assertEquals(InteractionType.ATTACK, command.getInteractionType());
        assertEquals(InteractionType.ATTACK.getDefaultPriority(), command.getPriority());
        assertTrue(command.requiresPositioning());
        assertFalse(command.isComplete());
        assertFalse(command.isCancelled());
    }
    
    @Test
    void shouldCreateWithCustomPriority() {
        TargetEntity fakeTarget = new FakeTargetEntity("zombie");
        
        AttackTargetCommand command = new AttackTargetCommand(fakeTarget, 8);
        
        assertEquals(8, command.getPriority());
    }
    
    @Test
    void shouldCompleteWhenTargetDies() {
        FakeTargetEntity fakeTarget = new FakeTargetEntity("zombie");
        fakeTarget.setAlive(false);
        
        AttackTargetCommand command = new AttackTargetCommand(fakeTarget);
        
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldCompleteWhenTargetIsNull() {
        AttackTargetCommand command = new AttackTargetCommand(null);
        
        command.execute();
        
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldCancelCorrectly() {
        TargetEntity fakeTarget = new FakeTargetEntity("zombie");
        
        AttackTargetCommand command = new AttackTargetCommand(fakeTarget);
        
        assertFalse(command.isCancelled());
        assertFalse(command.isComplete());
        
        command.cancel();
        
        assertTrue(command.isCancelled());
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldExecuteWithoutError() {
        TargetEntity fakeTarget = new FakeTargetEntity("zombie");
        
        AttackTargetCommand command = new AttackTargetCommand(fakeTarget);
        
        assertDoesNotThrow(() -> command.execute());
    }
    
    @Test
    void shouldBeCompleteWhenCancelled() {
        TargetEntity fakeTarget = new FakeTargetEntity("zombie");
        
        AttackTargetCommand command = new AttackTargetCommand(fakeTarget);
        command.cancel();
        
        command.execute();
        
        assertTrue(command.isComplete());
    }
    
    @Test
    void shouldBeCompleteWhenTargetBecomesInvalid() {
        FakeTargetEntity fakeTarget = new FakeTargetEntity("zombie");
        
        AttackTargetCommand command = new AttackTargetCommand(fakeTarget);
        
        assertFalse(command.isComplete());
        
        // Target dies
        fakeTarget.setAlive(false);
        
        assertTrue(command.isComplete());
    }
    
    /**
     * Test implementation of TargetEntity that can be controlled for testing.
     */
    private static class FakeTargetEntity implements TargetEntity {
        private final String entityType;
        private boolean alive = true;
        
        public FakeTargetEntity(String entityType) {
            this.entityType = entityType;
        }
        
        @Override
        public boolean isAlive() {
            return alive;
        }
        
        public void setAlive(boolean alive) {
            this.alive = alive;
        }
        
        @Override
        public net.minecraft.util.math.Vec3d getPosition() {
            return new net.minecraft.util.math.Vec3d(0, 0, 0);
        }
        
        @Override
        public String getEntityId() {
            return "test-" + entityType;
        }
        
        @Override
        public String getEntityType() {
            return entityType;
        }
        
        @Override
        public java.util.UUID getEntityUuid() {
            return java.util.UUID.randomUUID();
        }
    }
}