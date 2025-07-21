package com.aimobs.entity.ai.core;

/**
 * Core interface defining actions an entity can perform.
 * Part of the core layer - no dependencies on other layers.
 * 
 * Following Ben Johnson's standard package layout:
 * - Core layer contains only interfaces and value objects
 * - No dependencies on infrastructure or application layers
 */
public interface EntityActions {
    
    /**
     * Clear all current goals for this entity.
     */
    void clearGoals();
    
    /**
     * Add a swimming goal to prevent drowning.
     */
    void addSwimGoal();
    
    /**
     * Add an escape danger goal for survival.
     */
    void addEscapeDangerGoal();
    
    /**
     * Add a controllable goal for AI command processing.
     */
    void addControllableGoal();
    
    /**
     * Get the current position of this entity.
     * 
     * @return The entity's current position in the world
     */
    net.minecraft.util.math.Vec3d getPosition();
    
    /**
     * Add an interaction goal with the specified priority.
     * 
     * @param priority The priority level for the goal
     * @param goal The goal implementation to add
     */
    void addInteractionGoal(int priority, Object goal);
    
    /**
     * Remove a specific interaction goal.
     * 
     * @param goal The goal to remove
     */
    void removeInteractionGoal(Object goal);
    
    /**
     * Check if the entity can reach the specified target position.
     * 
     * @param targetPos The target position to check
     * @return True if the position is reachable
     */
    boolean canReachPosition(net.minecraft.util.math.Vec3d targetPos);
    
    /**
     * Get the world instance this entity exists in.
     * 
     * @return The world instance
     */
    net.minecraft.world.World getWorld();
    
    /**
     * Get the underlying wolf entity for infrastructure layer operations.
     * This provides a seam for infrastructure components that need direct access
     * to Minecraft's WolfEntity while maintaining clean architecture boundaries.
     * 
     * @return The underlying wolf entity implementation
     */
    net.minecraft.entity.passive.WolfEntity getWolfEntity();
}