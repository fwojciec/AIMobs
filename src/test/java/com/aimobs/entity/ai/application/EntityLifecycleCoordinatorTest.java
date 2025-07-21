package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.EntityLifecycleService;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AiEntityState;
import com.aimobs.entity.ai.core.AIState;
import com.aimobs.test.BaseUnitTest;
import com.aimobs.test.FakeAiPersistenceService;
import com.aimobs.test.FakeMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for EntityLifecycleCoordinator business logic.
 * Tests service coordination using fake objects - fast, deterministic tests.
 * 
 * Follows architecture principles: test business logic through interfaces.
 */
class EntityLifecycleCoordinatorTest extends BaseUnitTest {

    private EntityLifecycleService lifecycleService;
    private FakeAiPersistenceService fakePersistence;
    private FakeMessageService fakeMessageService;

    @BeforeEach
    void setUp() {
        fakePersistence = new FakeAiPersistenceService();
        fakeMessageService = new FakeMessageService();
        
        lifecycleService = new EntityLifecycleCoordinator(fakePersistence);
    }

    @Test
    void shouldRegisterAiEntity() {
        EntityId entityId = EntityId.generate();

        lifecycleService.registerAiEntity(entityId);

        assertThat(lifecycleService.isAiEntity(entityId)).isTrue();
        assertThat(fakePersistence.hasAiState(entityId)).isTrue();
        
        AiEntityState state = fakePersistence.loadAiState(entityId);
        assertThat(state.isAiControlled()).isTrue();
        assertThat(state.getCurrentState()).isEqualTo(AIState.IDLE);
        assertThat(state.getEntityType()).isEqualTo("ai_controlled_wolf");
    }

    @Test
    void shouldUnregisterAiEntity() {
        EntityId entityId = EntityId.generate();
        lifecycleService.registerAiEntity(entityId);
        assertThat(lifecycleService.isAiEntity(entityId)).isTrue();

        lifecycleService.unregisterAiEntity(entityId);

        assertThat(lifecycleService.isAiEntity(entityId)).isFalse();
        assertThat(fakePersistence.hasAiState(entityId)).isFalse();
    }

    @Test
    void shouldReconnectAiEntities() {
        // Setup: Create some persisted AI entities (simulating world reload scenario)
        EntityId entityId1 = EntityId.generate();
        EntityId entityId2 = EntityId.generate();
        EntityId nonAiEntityId = EntityId.generate();
        
        AiEntityState aiState = AiEntityState.createAiControlled(AIState.MOVING, "ai_controlled_wolf");
        AiEntityState nonAiState = AiEntityState.createNonAi("regular_wolf");
        
        fakePersistence.saveAiState(entityId1, aiState);
        fakePersistence.saveAiState(entityId2, aiState);
        fakePersistence.saveAiState(nonAiEntityId, nonAiState);

        // Act: Reconnect AI entities (simulating world load event)
        lifecycleService.reconnectAiEntities();

        // Assert: Only AI-controlled entities should be registered
        assertThat(lifecycleService.isAiEntity(entityId1)).isTrue();
        assertThat(lifecycleService.isAiEntity(entityId2)).isTrue();
        assertThat(lifecycleService.isAiEntity(nonAiEntityId)).isFalse(); // Non-AI should not be registered
    }

    @Test
    void shouldHandleEmptyPersistenceOnReconnect() {
        // No persisted entities
        assertThat(fakePersistence.getAllAiEntityIds()).hasSize(0);

        // Should not fail with empty persistence
        lifecycleService.reconnectAiEntities();

        // No entities should be registered
        assertThat(fakePersistence.getPersistedEntityCount()).isEqualTo(0);
    }

    @Test
    void shouldPreserveStateAfterReconnection() {
        EntityId entityId = EntityId.generate();
        AiEntityState originalState = AiEntityState.createAiControlled(AIState.ATTACKING, "ai_controlled_wolf");
        fakePersistence.saveAiState(entityId, originalState);

        lifecycleService.reconnectAiEntities();

        AiEntityState preservedState = fakePersistence.loadAiState(entityId);
        assertThat(preservedState).isEqualTo(originalState);
        assertThat(preservedState.getCurrentState()).isEqualTo(AIState.ATTACKING);
    }

    @Test
    void shouldIdentifyAiEntitiesFromPersistenceOrRegistry() {
        EntityId registeredEntity = EntityId.generate();
        EntityId persistedEntity = EntityId.generate();
        EntityId unknownEntity = EntityId.generate();

        // Register one entity (in-memory)
        lifecycleService.registerAiEntity(registeredEntity);
        
        // Add one directly to persistence (simulating loaded from save)
        AiEntityState state = AiEntityState.createAiControlled(AIState.IDLE, "ai_controlled_wolf");
        fakePersistence.saveAiState(persistedEntity, state);

        assertThat(lifecycleService.isAiEntity(registeredEntity)).isTrue();
        assertThat(lifecycleService.isAiEntity(persistedEntity)).isTrue();
        assertThat(lifecycleService.isAiEntity(unknownEntity)).isFalse();
    }
}