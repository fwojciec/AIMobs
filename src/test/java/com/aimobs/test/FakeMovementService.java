package com.aimobs.test;

import com.aimobs.entity.ai.MovementService;
import com.aimobs.entity.ai.core.MovementState;
import com.aimobs.entity.ai.core.MovementTarget;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Fake implementation of MovementService for testing.
 * Follows the fake object pattern used throughout the test suite.
 */
public class FakeMovementService implements MovementService {
    
    private MovementState currentState = MovementState.IDLE;
    private MovementTarget currentTarget;
    private PlayerEntity followTarget;
    private PlayerEntity comeHereTarget;
    private int moveToCallCount = 0;
    private int followPlayerCallCount = 0;
    private int comeHereCallCount = 0;
    private int stopCallCount = 0;
    private int updateProgressCallCount = 0;
    
    @Override
    public void moveTo(MovementTarget target) {
        moveToCallCount++;
        currentTarget = target;
        currentState = MovementState.MOVING_TO_LOCATION;
    }
    
    @Override
    public void followPlayer(PlayerEntity player) {
        followPlayerCallCount++;
        followTarget = player;
        currentState = MovementState.FOLLOWING_PLAYER;
    }
    
    @Override
    public void comeHere(PlayerEntity player) {
        comeHereCallCount++;
        comeHereTarget = player;
        currentState = MovementState.MOVING_TO_LOCATION;
    }
    
    @Override
    public void stop() {
        stopCallCount++;
        currentState = MovementState.IDLE;
        currentTarget = null;
        followTarget = null;
        comeHereTarget = null;
    }
    
    @Override
    public void updateMovementProgress() {
        updateProgressCallCount++;
    }
    
    @Override
    public MovementState getCurrentState() {
        return currentState;
    }
    
    @Override
    public MovementTarget getCurrentTarget() {
        return currentTarget;
    }
    
    // Test helper methods
    public int getMoveToCallCount() {
        return moveToCallCount;
    }
    
    public int getFollowPlayerCallCount() {
        return followPlayerCallCount;
    }
    
    public int getComeHereCallCount() {
        return comeHereCallCount;
    }
    
    public int getStopCallCount() {
        return stopCallCount;
    }
    
    public int getUpdateProgressCallCount() {
        return updateProgressCallCount;
    }
    
    public PlayerEntity getFollowTarget() {
        return followTarget;
    }
    
    public PlayerEntity getComeHereTarget() {
        return comeHereTarget;
    }
    
    public void setState(MovementState state) {
        currentState = state;
    }
    
    public void setTarget(MovementTarget target) {
        currentTarget = target;
    }
    
    public void reset() {
        currentState = MovementState.IDLE;
        currentTarget = null;
        followTarget = null;
        comeHereTarget = null;
        moveToCallCount = 0;
        followPlayerCallCount = 0;
        comeHereCallCount = 0;
        stopCallCount = 0;
        updateProgressCallCount = 0;
    }
}