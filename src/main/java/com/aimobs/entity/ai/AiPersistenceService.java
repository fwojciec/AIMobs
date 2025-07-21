package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AiEntityState;

/**
 * Service contract for persisting and restoring AI entity state.
 * Abstracts the storage mechanism (NBT, database, etc.) from business logic.
 * 
 * Root interface - defines what the system does (contract only).
 * Dependencies: Core layer only.
 */
public interface AiPersistenceService {
    
    /**
     * Saves the AI state for an entity.
     * 
     * @param entityId The unique identifier for the entity
     * @param state The AI state to persist
     */
    void saveAiState(EntityId entityId, AiEntityState state);
    
    /**
     * Loads the AI state for an entity.
     * 
     * @param entityId The unique identifier for the entity
     * @return The persisted AI state, or null if none exists
     */
    AiEntityState loadAiState(EntityId entityId);
    
    /**
     * Checks if an entity has persisted AI state.
     * 
     * @param entityId The entity to check
     * @return true if AI state exists for this entity
     */
    boolean hasAiState(EntityId entityId);
    
    /**
     * Removes the persisted AI state for an entity.
     * Called when an entity is permanently removed.
     * 
     * @param entityId The entity whose state should be removed
     */
    void removeAiState(EntityId entityId);
    
    /**
     * Gets all entity IDs that have persisted AI state.
     * Used during world load to find entities that need reconnection.
     * 
     * @return Array of entity IDs with persisted AI state
     */
    EntityId[] getAllAiEntityIds();
}