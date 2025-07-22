package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.AiControlledWolfEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service contract for finding and resolving command receivers.
 * Abstracts entity lookup from platform-specific implementation.
 * 
 * Root interface - defines what the system does (contract only).
 * Dependencies: Core layer only.
 */
public interface EntityLookupService {
    
    /**
     * Finds a specific command receiver by its unique identifier.
     * Used for routing commands to specific entities after world reload.
     * 
     * @param entityId The unique identifier of the entity
     * @return Optional containing the command receiver, or empty if not found
     */
    Optional<CommandReceiver> findEntityById(EntityId entityId);
    
    /**
     * Gets all available command receivers in the current context.
     * Returns entities that are ready to process commands.
     * 
     * @return List of available command receivers
     */
    List<CommandReceiver> getAllAvailableEntities();
    
    /**
     * Finds any available command receiver for command processing.
     * Used when commands don't target a specific entity.
     * 
     * @return Optional containing an available receiver, or empty if none found
     */
    Optional<CommandReceiver> findAnyAvailable();
    
    /**
     * Gets the count of available command receivers.
     * Used for monitoring and load balancing decisions.
     * 
     * @return Number of available receivers
     */
    int getAvailableEntityCount();
    
    /**
     * Finds a specific AI wolf by its UUID.
     * Used by feedback system to locate wolves for visual/audio effects.
     * 
     * @param wolfId The unique identifier of the wolf
     * @return The wolf entity, or null if not found
     */
    AiControlledWolfEntity findWolfById(UUID wolfId);
}