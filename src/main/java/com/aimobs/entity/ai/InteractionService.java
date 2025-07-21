package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.AIState;
import com.aimobs.entity.ai.core.TargetEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Service contract for entity interaction coordination.
 * Provides high-level interaction operations while abstracting
 * the underlying goal management and execution implementation.
 * 
 * Following Ben Johnson's standard package layout:
 * - Root interface defining service contract
 * - No implementation details - only behavior contracts
 */
public interface InteractionService {
    
    /**
     * Initiates an attack on the specified target entity.
     * 
     * @param target The entity to attack
     */
    void attackTarget(TargetEntity target);
    
    /**
     * Starts collecting items of the specified type within a radius.
     * 
     * @param itemType The type of items to collect (e.g., "wood", "stone", "all")
     * @param radius The radius within which to search for items
     * @param maxItems Maximum number of items to collect (0 for unlimited)
     */
    void collectItems(String itemType, double radius, int maxItems);
    
    /**
     * Begins defending the specified area from hostile entities.
     * 
     * @param centerPos The center position of the area to defend
     * @param radius The radius of the area to defend
     */
    void defendArea(BlockPos centerPos, double radius);
    
    /**
     * Processes a communication command and generates appropriate response.
     * 
     * @param message The communication message to process
     * @return A response message, or null if no response needed
     */
    String processCommunication(String message);
    
    /**
     * Coordinates positioning with movement system for optimal interaction.
     * Called by interaction goals to request positioning assistance.
     * 
     * @param targetPosition The position needed for interaction
     */
    void coordinatePositioning(Vec3d targetPosition);
    
    /**
     * Immediately stops all current interactions and clears active goals.
     */
    void stopAllInteractions();
    
    /**
     * Updates interaction progress and handles state transitions.
     * Should be called regularly (e.g., in entity tick) to maintain
     * accurate interaction state.
     */
    void updateInteractionProgress();
    
    /**
     * @return The current interaction state
     */
    AIState getCurrentState();
    
    /**
     * @return True if currently engaged in any interaction
     */
    boolean isInteracting();
    
    /**
     * @return The current interaction target position, or null if no active interaction
     */
    Vec3d getCurrentTargetPosition();
}