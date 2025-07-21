package com.aimobs.entity.ai.core;

import com.aimobs.entity.ai.MovementService;

/**
 * Command to immediately stop all movement.
 * Implements the Command pattern for movement cessation.
 */
public final class StopMovementCommand implements AICommand {
    private final MovementService movementService;
    private boolean executed = false;

    public StopMovementCommand(MovementService movementService) {
        this.movementService = movementService;
    }

    @Override
    public void execute() {
        movementService.stop();
        executed = true;
    }

    @Override
    public boolean isComplete() {
        return executed;
    }

    @Override
    public void cancel() {
        // Stop commands cannot be cancelled - they execute immediately
    }
}