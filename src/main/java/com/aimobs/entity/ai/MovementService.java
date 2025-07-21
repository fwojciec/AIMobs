package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.MovementState;
import com.aimobs.entity.ai.core.MovementTarget;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Service contract for entity movement coordination.
 * Provides high-level movement operations while abstracting
 * the underlying pathfinding and navigation implementation.
 */
public interface MovementService {
    
    /**
     * Moves the entity to the specified target location.
     * 
     * @param target The destination to move to
     */
    void moveTo(MovementTarget target);
    
    /**
     * Makes the entity continuously follow the specified player.
     * 
     * @param player The player to follow
     */
    void followPlayer(PlayerEntity player);
    
    /**
     * Moves the entity to the player's current location.
     * This is a one-time movement to where the player is now.
     * 
     * @param player The player whose location to move to
     */
    void comeHere(PlayerEntity player);
    
    /**
     * Immediately stops all movement and clears current targets.
     */
    void stop();
    
    /**
     * Updates movement progress and handles state transitions.
     * Should be called regularly (e.g., in entity tick) to maintain
     * accurate movement state.
     */
    void updateMovementProgress();
    
    /**
     * @return The current movement state
     */
    MovementState getCurrentState();
    
    /**
     * @return The current movement target, or null if no active movement
     */
    MovementTarget getCurrentTarget();
}