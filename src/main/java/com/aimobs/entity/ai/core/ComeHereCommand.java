package com.aimobs.entity.ai.core;

import com.aimobs.entity.ai.MovementService;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Command to make an entity move to the player's current location.
 * Implements the Command pattern for "come here" behavior.
 */
public final class ComeHereCommand implements AICommand {
    private final MovementService movementService;
    private final PlayerEntity player;
    private boolean executed = false;
    private boolean cancelled = false;

    public ComeHereCommand(MovementService movementService, PlayerEntity player) {
        this.movementService = movementService;
        this.player = player;
    }

    @Override
    public void execute() {
        if (cancelled) {
            return;
        }
        movementService.comeHere(player);
        executed = true;
    }

    @Override
    public boolean isComplete() {
        if (cancelled) {
            return true;
        }
        return executed && (movementService.getCurrentState() == MovementState.IDLE || 
                           movementService.getCurrentState() == MovementState.PATHFINDING_FAILED);
    }

    @Override
    public void cancel() {
        cancelled = true;
        if (executed) {
            movementService.stop();
        }
    }

    public PlayerEntity getPlayer() {
        return player;
    }
}