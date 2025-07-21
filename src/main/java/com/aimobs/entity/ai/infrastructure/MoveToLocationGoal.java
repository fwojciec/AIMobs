package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.MovementService;
import com.aimobs.entity.ai.core.MovementState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.WolfEntity;

import java.util.EnumSet;

/**
 * Minecraft AI Goal that integrates movement commands with the entity's goal system.
 * Serves as a bridge between our movement service and Minecraft's AI goal framework.
 */
public class MoveToLocationGoal extends Goal {
    
    private final WolfEntity wolf;
    private final MovementService movementService;

    public MoveToLocationGoal(WolfEntity wolf, MovementService movementService) {
        this.wolf = wolf;
        this.movementService = movementService;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        // This goal can start when there's an active movement command
        MovementState state = movementService.getCurrentState();
        return state == MovementState.MOVING_TO_LOCATION || 
               state == MovementState.FOLLOWING_PLAYER;
    }

    @Override
    public boolean shouldContinue() {
        // Continue while movement is active
        MovementState state = movementService.getCurrentState();
        return state == MovementState.MOVING_TO_LOCATION || 
               state == MovementState.FOLLOWING_PLAYER;
    }

    @Override
    public void start() {
        // Goal is starting - movement service already has the target
        // Just ensure we're in the right state
    }

    @Override
    public void tick() {
        // Update movement progress on each tick
        movementService.updateMovementProgress();
    }

    @Override
    public void stop() {
        // Goal is stopping - this might be due to interruption by another goal
        // Let the movement service handle cleanup if needed
    }

    @Override
    public boolean canStop() {
        // Allow other goals to interrupt movement if needed
        return true;
    }
}