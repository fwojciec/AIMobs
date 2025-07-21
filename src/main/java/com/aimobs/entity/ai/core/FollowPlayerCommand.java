package com.aimobs.entity.ai.core;

import com.aimobs.entity.ai.MovementService;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Command to make an entity follow a player.
 * Implements the Command pattern for player following behavior.
 */
public final class FollowPlayerCommand implements AICommand {
    private final MovementService movementService;
    private final PlayerEntity player;
    private boolean executed = false;
    private boolean cancelled = false;

    public FollowPlayerCommand(MovementService movementService, PlayerEntity player) {
        this.movementService = movementService;
        this.player = player;
    }

    @Override
    public void execute() {
        if (cancelled) {
            return;
        }
        movementService.followPlayer(player);
        executed = true;
    }

    @Override
    public boolean isComplete() {
        if (cancelled) {
            return true;
        }
        // Following is continuous, only complete if cancelled or failed
        return executed && movementService.getCurrentState() == MovementState.PATHFINDING_FAILED;
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