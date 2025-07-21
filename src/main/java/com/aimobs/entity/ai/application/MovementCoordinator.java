package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.MovementService;
import com.aimobs.entity.ai.PathfindingService;
import com.aimobs.entity.ai.core.EntityActions;
import com.aimobs.entity.ai.core.MovementState;
import com.aimobs.entity.ai.core.MovementTarget;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Application service implementing movement coordination logic.
 * Orchestrates between high-level movement commands and low-level pathfinding.
 */
public class MovementCoordinator implements MovementService {
    
    private final EntityActions entityActions;
    private final PathfindingService pathfindingService;
    
    private MovementState currentState = MovementState.IDLE;
    private MovementTarget currentTarget = null;
    private PlayerEntity followingPlayer = null;

    public MovementCoordinator(EntityActions entityActions, PathfindingService pathfindingService) {
        this.entityActions = entityActions;
        this.pathfindingService = pathfindingService;
    }

    @Override
    public void moveTo(MovementTarget target) {
        if (target == null) {
            return;
        }
        
        System.out.println("[AIMobs] MovementCoordinator.moveTo called - target: " + target + ", current pos: " + entityActions.getPosition());
        
        // Stop any current movement
        stopCurrentMovement();
        
        // Check if target is reachable
        boolean canReach = pathfindingService.canReachTarget(entityActions.getPosition(), target);
        System.out.println("[AIMobs] Pathfinding canReachTarget result: " + canReach);
        if (!canReach) {
            currentState = MovementState.PATHFINDING_FAILED;
            System.out.println("[AIMobs] MovementCoordinator setting state to PATHFINDING_FAILED");
            return;
        }
        
        // Start pathfinding
        System.out.println("[AIMobs] Starting pathfinding...");
        pathfindingService.startPathfinding(entityActions.getPosition(), target);
        currentTarget = target;
        currentState = MovementState.MOVING_TO_LOCATION;
        followingPlayer = null;
        System.out.println("[AIMobs] MovementCoordinator state set to MOVING_TO_LOCATION");
    }

    @Override
    public void followPlayer(PlayerEntity player) {
        if (player == null) {
            return;
        }
        
        // Stop any current movement
        stopCurrentMovement();
        
        followingPlayer = player;
        currentState = MovementState.FOLLOWING_PLAYER;
        currentTarget = null;
        
        // Start following by moving to player's current position
        MovementTarget playerTarget = new MovementTarget(player.getBlockPos());
        pathfindingService.startPathfinding(entityActions.getPosition(), playerTarget);
    }

    @Override
    public void comeHere(PlayerEntity player) {
        if (player == null) {
            return;
        }
        
        MovementTarget playerTarget = new MovementTarget(player.getBlockPos());
        moveTo(playerTarget);
    }

    @Override
    public void stop() {
        stopCurrentMovement();
    }

    @Override
    public void updateMovementProgress() {
        switch (currentState) {
            case MOVING_TO_LOCATION:
                updateLocationMovement();
                break;
            case FOLLOWING_PLAYER:
                updatePlayerFollowing();
                break;
            case IDLE:
            case PATHFINDING_FAILED:
                // No updates needed for these states
                break;
        }
    }

    @Override
    public MovementState getCurrentState() {
        return currentState;
    }

    @Override
    public MovementTarget getCurrentTarget() {
        return currentTarget;
    }

    private void updateLocationMovement() {
        if (pathfindingService.hasPathfindingFailed()) {
            currentState = MovementState.PATHFINDING_FAILED;
            currentTarget = null;
            return;
        }
        
        if (pathfindingService.hasReachedTarget() || !pathfindingService.isMoving()) {
            // Check if we're actually at the target
            if (currentTarget != null && 
                currentTarget.distanceFrom(entityActions.getPosition()) <= 2.0) {
                // Successfully reached target
                currentState = MovementState.IDLE;
                currentTarget = null;
            } else if (!pathfindingService.isMoving()) {
                // Stopped moving but didn't reach target - likely failed
                currentState = MovementState.PATHFINDING_FAILED;
                currentTarget = null;
            }
        }
    }

    private void updatePlayerFollowing() {
        if (followingPlayer == null) {
            currentState = MovementState.IDLE;
            return;
        }
        
        // Check if player moved significantly
        double distanceToPlayer = followingPlayer.getPos().distanceTo(entityActions.getPosition());
        
        if (distanceToPlayer > 5.0) {
            // Player is too far, update following target
            MovementTarget newPlayerTarget = new MovementTarget(followingPlayer.getBlockPos());
            pathfindingService.startPathfinding(entityActions.getPosition(), newPlayerTarget);
        } else if (distanceToPlayer < 2.0 && pathfindingService.isMoving()) {
            // Close enough to player, can stop moving
            pathfindingService.stopPathfinding();
        }
        
        if (pathfindingService.hasPathfindingFailed()) {
            currentState = MovementState.PATHFINDING_FAILED;
            followingPlayer = null;
        }
    }

    private void stopCurrentMovement() {
        pathfindingService.stopPathfinding();
        currentState = MovementState.IDLE;
        currentTarget = null;
        followingPlayer = null;
    }
}