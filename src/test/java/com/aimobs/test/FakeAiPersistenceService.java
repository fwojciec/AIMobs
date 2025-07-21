package com.aimobs.test;

import com.aimobs.entity.ai.AiPersistenceService;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AiEntityState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Fake implementation of AiPersistenceService for testing.
 * Provides predictable behavior for unit tests without external dependencies.
 * 
 * Follows the fake object pattern used throughout the test suite.
 */
public class FakeAiPersistenceService implements AiPersistenceService {
    
    private final Map<EntityId, AiEntityState> persistedStates = new ConcurrentHashMap<>();
    private boolean simulateFailure = false;

    @Override
    public void saveAiState(EntityId entityId, AiEntityState state) {
        if (simulateFailure) {
            throw new RuntimeException("Simulated persistence failure");
        }
        persistedStates.put(entityId, state);
    }

    @Override
    public AiEntityState loadAiState(EntityId entityId) {
        if (simulateFailure) {
            throw new RuntimeException("Simulated persistence failure");
        }
        return persistedStates.get(entityId);
    }

    @Override
    public boolean hasAiState(EntityId entityId) {
        return persistedStates.containsKey(entityId);
    }

    @Override
    public void removeAiState(EntityId entityId) {
        persistedStates.remove(entityId);
    }

    @Override
    public EntityId[] getAllAiEntityIds() {
        return persistedStates.keySet().toArray(new EntityId[0]);
    }

    // Test control methods
    public void simulateFailure(boolean shouldFail) {
        this.simulateFailure = shouldFail;
    }

    public void reset() {
        persistedStates.clear();
        simulateFailure = false;
    }

    public int getPersistedEntityCount() {
        return persistedStates.size();
    }

    public boolean wasEntityPersisted(EntityId entityId) {
        return persistedStates.containsKey(entityId);
    }
}