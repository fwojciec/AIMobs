package com.aimobs.test;

import com.aimobs.entity.ai.InteractionService;
import com.aimobs.entity.ai.core.AIState;
import com.aimobs.entity.ai.core.TargetEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Fake implementation of InteractionService for testing.
 * Follows the fake object pattern used throughout the test suite.
 */
public class FakeInteractionService implements InteractionService {
    
    private AIState currentState = AIState.IDLE;
    private Vec3d currentTargetPosition;
    private String lastCommunicationMessage;
    private String lastCommunicationResponse;
    private TargetEntity lastAttackTarget;
    private String lastCollectItemType;
    private double lastCollectRadius;
    private int lastCollectMaxItems;
    private BlockPos lastDefendCenter;
    private double lastDefendRadius;
    private boolean stopAllInteractionsCalled = false;
    private Vec3d lastCoordinatePositionRequest;
    private int updateProgressCallCount = 0;
    
    @Override
    public void attackTarget(TargetEntity target) {
        lastAttackTarget = target;
        currentState = AIState.ATTACKING;
        if (target != null) {
            currentTargetPosition = target.getPosition();
        }
    }
    
    @Override
    public void collectItems(String itemType, double radius, int maxItems) {
        lastCollectItemType = itemType;
        lastCollectRadius = radius;
        lastCollectMaxItems = maxItems;
        currentState = AIState.COLLECTING;
    }
    
    @Override
    public void defendArea(BlockPos centerPos, double radius) {
        lastDefendCenter = centerPos;
        lastDefendRadius = radius;
        currentState = AIState.DEFENDING;
        if (centerPos != null) {
            currentTargetPosition = new Vec3d(centerPos.getX(), centerPos.getY(), centerPos.getZ());
        }
    }
    
    @Override
    public String processCommunication(String message) {
        lastCommunicationMessage = message;
        
        // Generate predictable responses for testing
        if (message == null || message.trim().isEmpty()) {
            lastCommunicationResponse = null;
        } else if (message.toLowerCase().contains("status")) {
            lastCommunicationResponse = "Status: " + currentState.name().toLowerCase();
        } else if (message.toLowerCase().contains("hello")) {
            lastCommunicationResponse = "Hello there!";
        } else {
            lastCommunicationResponse = "I heard: " + message;
        }
        
        return lastCommunicationResponse;
    }
    
    @Override
    public void coordinatePositioning(Vec3d targetPosition) {
        lastCoordinatePositionRequest = targetPosition;
    }
    
    @Override
    public void stopAllInteractions() {
        stopAllInteractionsCalled = true;
        currentState = AIState.IDLE;
        currentTargetPosition = null;
    }
    
    @Override
    public void updateInteractionProgress() {
        updateProgressCallCount++;
    }
    
    @Override
    public AIState getCurrentState() {
        return currentState;
    }
    
    @Override
    public boolean isInteracting() {
        return currentState != AIState.IDLE;
    }
    
    @Override
    public Vec3d getCurrentTargetPosition() {
        return currentTargetPosition;
    }
    
    // Test helper methods
    public String getLastCommunicationMessage() {
        return lastCommunicationMessage;
    }
    
    public String getLastCommunicationResponse() {
        return lastCommunicationResponse;
    }
    
    public TargetEntity getLastAttackTarget() {
        return lastAttackTarget;
    }
    
    public String getLastCollectItemType() {
        return lastCollectItemType;
    }
    
    public double getLastCollectRadius() {
        return lastCollectRadius;
    }
    
    public int getLastCollectMaxItems() {
        return lastCollectMaxItems;
    }
    
    public BlockPos getLastDefendCenter() {
        return lastDefendCenter;
    }
    
    public double getLastDefendRadius() {
        return lastDefendRadius;
    }
    
    public boolean wasStopAllInteractionsCalled() {
        return stopAllInteractionsCalled;
    }
    
    public Vec3d getLastCoordinatePositionRequest() {
        return lastCoordinatePositionRequest;
    }
    
    public int getUpdateProgressCallCount() {
        return updateProgressCallCount;
    }
    
    public void setState(AIState state) {
        currentState = state;
    }
    
    public void setTargetPosition(Vec3d position) {
        currentTargetPosition = position;
    }
    
    public void reset() {
        currentState = AIState.IDLE;
        currentTargetPosition = null;
        lastCommunicationMessage = null;
        lastCommunicationResponse = null;
        lastAttackTarget = null;
        lastCollectItemType = null;
        lastCollectRadius = 0;
        lastCollectMaxItems = 0;
        lastDefendCenter = null;
        lastDefendRadius = 0;
        stopAllInteractionsCalled = false;
        lastCoordinatePositionRequest = null;
        updateProgressCallCount = 0;
    }
}