package com.aimobs.entity.ai.core;

import com.aimobs.entity.ai.MovementService;

/**
 * Command to move an entity to a specific location.
 * Implements the Command pattern for movement actions.
 */
public final class MoveToLocationCommand implements AICommand {
    private final MovementService movementService;
    private final MovementTarget target;
    private boolean executed = false;
    private boolean cancelled = false;

    public MoveToLocationCommand(MovementService movementService, MovementTarget target) {
        this.movementService = movementService;
        this.target = target;
    }

    @Override
    public void execute() {
        if (cancelled) {
            return;
        }
        System.out.println("[AIMobs] MoveToLocationCommand executing - target: " + target);
        movementService.moveTo(target);
        System.out.println("[AIMobs] MoveToLocationCommand called moveTo, movement state: " + movementService.getCurrentState());
        executed = true;
    }

    @Override
    public boolean isComplete() {
        if (cancelled) {
            return true;
        }
        boolean complete = executed && (movementService.getCurrentState() == MovementState.IDLE || 
                           movementService.getCurrentState() == MovementState.PATHFINDING_FAILED);
        if (complete && executed) {
            System.out.println("[AIMobs] MoveToLocationCommand completing - final state: " + movementService.getCurrentState());
        }
        return complete;
    }

    @Override
    public void cancel() {
        cancelled = true;
        if (executed) {
            movementService.stop();
        }
    }

    public MovementTarget getTarget() {
        return target;
    }
}