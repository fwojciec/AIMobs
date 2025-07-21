package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.EntityLifecycleService;
import com.aimobs.entity.ai.AiPersistenceService;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AiEntityState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Application service implementing entity lifecycle coordination logic.
 * Orchestrates between persistence and command system to restore AI capabilities.
 * 
 * Pure business logic - no infrastructure dependencies.
 * Dependencies: Root interfaces + Core only.
 */
public class EntityLifecycleCoordinator implements EntityLifecycleService {
    
    private final AiPersistenceService persistenceService;
    private final Set<EntityId> registeredEntities;

    public EntityLifecycleCoordinator(AiPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        this.registeredEntities = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void reconnectAiEntities() {
        // Business logic: Find all AI entities and reconnect them
        EntityId[] aiEntityIds = persistenceService.getAllAiEntityIds();
        
        for (EntityId entityId : aiEntityIds) {
            AiEntityState state = persistenceService.loadAiState(entityId);
            
            if (state != null && state.isAiControlled()) {
                reconnectSingleEntity(entityId, state);
            }
        }
    }

    @Override
    public void registerAiEntity(EntityId entityId) {
        registeredEntities.add(entityId);
        
        // Save initial AI state
        AiEntityState initialState = AiEntityState.createAiControlled(
            com.aimobs.entity.ai.core.AIState.IDLE, 
            "ai_controlled_wolf"
        );
        persistenceService.saveAiState(entityId, initialState);
    }

    @Override
    public void unregisterAiEntity(EntityId entityId) {
        registeredEntities.remove(entityId);
        persistenceService.removeAiState(entityId);
    }

    @Override
    public boolean isAiEntity(EntityId entityId) {
        // Check if registered in current session
        if (registeredEntities.contains(entityId)) {
            return true;
        }
        
        // Check if persisted as AI-controlled entity
        AiEntityState state = persistenceService.loadAiState(entityId);
        return state != null && state.isAiControlled();
    }

    @Override
    public EntityId[] getAllAiEntityIds() {
        return persistenceService.getAllAiEntityIds();
    }

    /**
     * Reconnects a single AI entity to the command system.
     * Pure business logic for entity reconnection.
     */
    private void reconnectSingleEntity(EntityId entityId, AiEntityState state) {
        // Register with current session
        registeredEntities.add(entityId);
        
        // Entity lookup and MessageService reconnection will be handled by
        // the MessageParser when it searches for available wolves using findAvailableWolf()
        // This method just ensures the entity is registered and state is preserved
        
        // Ensure state is preserved
        persistenceService.saveAiState(entityId, state);
        
        System.out.println("[AIMobs] Reconnected AI entity: " + entityId.asString());
    }
}