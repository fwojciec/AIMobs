package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.EntityId;

/**
 * Service contract for managing AI entity lifecycle and reconnection.
 * Handles the business logic of reconnecting AI entities after world reload.
 * 
 * Root interface - defines what the system does (contract only).
 * Dependencies: Core layer only.
 */
public interface EntityLifecycleService {
    
    /**
     * Reconnects all AI-controlled entities to the command system.
     * Called when a world is loaded to restore AI capabilities.
     */
    void reconnectAiEntities();
    
    /**
     * Registers an AI entity with the lifecycle management system.
     * Should be called when an AI entity is spawned.
     * 
     * @param entityId The unique identifier for the entity
     */
    void registerAiEntity(EntityId entityId);
    
    /**
     * Unregisters an AI entity from the lifecycle management system.
     * Should be called when an AI entity is removed/dies.
     * 
     * @param entityId The unique identifier for the entity
     */
    void unregisterAiEntity(EntityId entityId);
    
    /**
     * Checks if an entity is registered as AI-controlled.
     * 
     * @param entityId The entity to check
     * @return true if the entity is registered as AI-controlled
     */
    boolean isAiEntity(EntityId entityId);
    
    /**
     * Gets all registered AI entity IDs.
     * 
     * @return Array of all registered AI entity IDs
     */
    EntityId[] getAllAiEntityIds();
}