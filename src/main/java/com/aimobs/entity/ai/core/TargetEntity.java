package com.aimobs.entity.ai.core;

import net.minecraft.util.math.Vec3d;

/**
 * Interface representing a target entity for interaction commands.
 * 
 * Part of the core layer - pure domain interface with minimal dependencies.
 * Following Ben Johnson's standard package layout principles.
 * 
 * This provides a seam for testing and decouples commands from Minecraft internals.
 */
public interface TargetEntity {
    
    /**
     * @return True if the entity is alive and can be targeted
     */
    boolean isAlive();
    
    /**
     * @return The current position of the entity
     */
    Vec3d getPosition();
    
    /**
     * @return A unique identifier for this entity
     */
    String getEntityId();
    
    /**
     * @return The type of entity (for display/logging purposes)
     */
    String getEntityType();
}