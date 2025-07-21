package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.InteractionService;
import com.aimobs.entity.ai.core.AIState;
import com.aimobs.test.BaseUnitTest;
import com.aimobs.test.FakeEntityActions;
import com.aimobs.test.FakeInventoryActions;
import com.aimobs.test.FakeMovementService;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InteractionCoordinator following TDD approach.
 * Tests business logic in isolation using fake objects.
 */
class InteractionCoordinatorTest extends BaseUnitTest {
    
    private InteractionService interactionService;
    private FakeEntityActions fakeEntityActions;
    private FakeInventoryActions fakeInventoryActions;
    private FakeMovementService fakeMovementService;
    
    @BeforeEach
    void setUp() {
        fakeEntityActions = new FakeEntityActions();
        fakeInventoryActions = new FakeInventoryActions();
        fakeMovementService = new FakeMovementService();
        
        interactionService = new InteractionCoordinator(
            fakeEntityActions, 
            fakeInventoryActions, 
            fakeMovementService
        );
    }
    
    @Test
    void shouldStartWithIdleState() {
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        assertFalse(interactionService.isInteracting());
        assertNull(interactionService.getCurrentTargetPosition());
    }
    
    @Test
    void shouldNotAttackNullTarget() {
        // Testing the coordinator's null handling behavior
        interactionService.attackTarget(null);
        
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        assertFalse(interactionService.isInteracting());
        assertEquals(0, fakeEntityActions.getInteractionGoalCount());
    }
    
    @Test
    void shouldCollectItemsWithValidParameters() {
        interactionService.collectItems("wood", 15.0, 10);
        
        assertEquals(AIState.COLLECTING, interactionService.getCurrentState());
        assertTrue(interactionService.isInteracting());
        assertEquals(1, fakeEntityActions.getInteractionGoalCount());
        assertNotNull(interactionService.getCurrentTargetPosition());
    }
    
    @Test
    void shouldNotCollectWithInvalidItemType() {
        interactionService.collectItems(null, 15.0, 10);
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        
        interactionService.collectItems("", 15.0, 10);
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        
        interactionService.collectItems("   ", 15.0, 10);
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        
        assertEquals(0, fakeEntityActions.getInteractionGoalCount());
    }
    
    @Test
    void shouldNotCollectWithInvalidRadius() {
        interactionService.collectItems("wood", 0, 10);
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        
        interactionService.collectItems("wood", -5.0, 10);
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        
        assertEquals(0, fakeEntityActions.getInteractionGoalCount());
    }
    
    @Test
    void shouldDefendAreaWithValidParameters() {
        BlockPos centerPos = new BlockPos(0, 64, 0);
        
        interactionService.defendArea(centerPos, 20.0);
        
        assertEquals(AIState.DEFENDING, interactionService.getCurrentState());
        assertTrue(interactionService.isInteracting());
        assertEquals(1, fakeEntityActions.getInteractionGoalCount());
        assertEquals(new Vec3d(0, 64, 0), interactionService.getCurrentTargetPosition());
    }
    
    @Test
    void shouldNotDefendWithInvalidParameters() {
        interactionService.defendArea(null, 20.0);
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        
        BlockPos centerPos = new BlockPos(0, 64, 0);
        interactionService.defendArea(centerPos, 0);
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        
        interactionService.defendArea(centerPos, -10.0);
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        
        assertEquals(0, fakeEntityActions.getInteractionGoalCount());
    }
    
    @Test
    void shouldProcessCommunicationStatusRequest() {
        String response = interactionService.processCommunication("What do you see?");
        
        assertNotNull(response);
        assertTrue(response.toLowerCase().contains("idle"));
        assertTrue(response.contains("(0.0, 64.0, 0.0)")); // Default position
    }
    
    @Test
    void shouldProcessCommunicationGreeting() {
        String response = interactionService.processCommunication("Hello");
        
        assertNotNull(response);
        assertTrue(response.toLowerCase().contains("hello") || 
                  response.toLowerCase().contains("ready"));
    }
    
    @Test
    void shouldProcessCommunicationInventoryRequest() {
        String response = interactionService.processCommunication("inventory");
        
        assertNotNull(response);
        assertTrue(response.contains("0/27")); // Empty inventory
    }
    
    @Test
    void shouldProcessCommunicationGenericMessage() {
        String response = interactionService.processCommunication("test message");
        
        assertNotNull(response);
        assertTrue(response.contains("test message"));
    }
    
    @Test
    void shouldReturnNullForEmptyCommunication() {
        assertNull(interactionService.processCommunication(null));
        assertNull(interactionService.processCommunication(""));
        assertNull(interactionService.processCommunication("   "));
    }
    
    @Test
    void shouldCoordinatePositioningWhenFarFromTarget() {
        Vec3d targetPosition = new Vec3d(10, 64, 10); // Far from default position but reachable
        
        interactionService.coordinatePositioning(targetPosition);
        
        // Should have requested movement assistance
        assertEquals(1, fakeMovementService.getMoveToCallCount());
    }
    
    @Test
    void shouldNotCoordinatePositioningWhenCloseToTarget() {
        Vec3d targetPosition = new Vec3d(2, 64, 2); // Close to default position
        
        interactionService.coordinatePositioning(targetPosition);
        
        // Should not request movement assistance
        assertEquals(0, fakeMovementService.getMoveToCallCount());
    }
    
    @Test
    void shouldNotCoordinatePositioningForNullTarget() {
        interactionService.coordinatePositioning(null);
        
        assertEquals(0, fakeMovementService.getMoveToCallCount());
    }
    
    @Test
    void shouldNotCoordinatePositioningForUnreachableTarget() {
        Vec3d targetPosition = new Vec3d(50, 64, 50); // Beyond reachable range
        
        interactionService.coordinatePositioning(targetPosition);
        
        // Should not request movement assistance for unreachable positions
        assertEquals(0, fakeMovementService.getMoveToCallCount());
    }
    
    @Test
    void shouldStopAllInteractions() {
        // Start an interaction (using collect items since it doesn't require LivingEntity)
        interactionService.collectItems("wood", 15.0, 10);
        
        assertTrue(interactionService.isInteracting());
        assertEquals(1, fakeEntityActions.getInteractionGoalCount());
        
        // Stop all interactions
        interactionService.stopAllInteractions();
        
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        assertFalse(interactionService.isInteracting());
        assertNull(interactionService.getCurrentTargetPosition());
        assertEquals(0, fakeEntityActions.getInteractionGoalCount());
    }
    
    @Test
    void shouldStopPreviousInteractionWhenStartingNew() {
        // Start first interaction
        interactionService.collectItems("stone", 15.0, 8);
        
        assertEquals(AIState.COLLECTING, interactionService.getCurrentState());
        assertEquals(1, fakeEntityActions.getInteractionGoalCount());
        
        // Start second interaction - should stop first
        interactionService.defendArea(new BlockPos(0, 64, 0), 20.0);
        
        assertEquals(AIState.DEFENDING, interactionService.getCurrentState());
        assertEquals(1, fakeEntityActions.getInteractionGoalCount()); // Still only one goal
    }
    
    @Test
    void shouldUpdateInteractionProgress() {
        // Test with no active interaction
        interactionService.updateInteractionProgress();
        assertEquals(AIState.IDLE, interactionService.getCurrentState());
        
        // Start an interaction
        interactionService.collectItems("wood", 15.0, 10);
        
        // Update progress - should maintain state
        interactionService.updateInteractionProgress();
        assertEquals(AIState.COLLECTING, interactionService.getCurrentState());
    }
    
    @Test
    void shouldReturnCorrectStateBasedOnContext() {
        // Test collecting state
        interactionService.collectItems("wood", 10.0, 5);
        String response = interactionService.processCommunication("how are you");
        assertTrue(response.toLowerCase().contains("collecting") || 
                  response.toLowerCase().contains("busy"));
        
        // Test defending state  
        interactionService.defendArea(new BlockPos(0, 64, 0), 15.0);
        response = interactionService.processCommunication("hello");
        assertTrue(response.toLowerCase().contains("guard") || 
                  response.toLowerCase().contains("defending"));
    }
    
    /**
     * We can't easily create a fake LivingEntity due to Minecraft's initialization requirements.
     * For testing the InteractionCoordinator, we'll just test with null and verify the coordinator
     * handles it correctly. The actual LivingEntity integration will be tested at a higher level.
     */
}