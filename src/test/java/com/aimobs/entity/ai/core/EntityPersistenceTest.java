package com.aimobs.entity.ai.core;

import com.aimobs.test.BaseUnitTest;
import com.aimobs.test.FakeAiPersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for AI entity persistence functionality.
 * Tests the core domain objects and persistence contract.
 * 
 * Fast tests with no external dependencies - follows architecture principles.
 */
class EntityPersistenceTest extends BaseUnitTest {

    private FakeAiPersistenceService persistenceService;

    @BeforeEach
    void setUp() {
        persistenceService = new FakeAiPersistenceService();
    }

    @Test
    void shouldCreateUniqueEntityIds() {
        EntityId id1 = EntityId.generate();
        EntityId id2 = EntityId.generate();

        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.getUuid()).isNotEqualTo(id2.getUuid());
    }

    @Test
    void shouldCreateEntityIdFromString() {
        UUID originalUuid = UUID.randomUUID();
        EntityId original = new EntityId(originalUuid);
        
        EntityId restored = EntityId.fromString(original.asString());
        
        assertThat(restored).isEqualTo(original);
        assertThat(restored.getUuid()).isEqualTo(originalUuid);
    }

    @Test
    void shouldSaveAndLoadAiState() {
        EntityId entityId = EntityId.generate();
        AiEntityState originalState = AiEntityState.createAiControlled(AIState.IDLE, "wolf");

        persistenceService.saveAiState(entityId, originalState);
        AiEntityState loadedState = persistenceService.loadAiState(entityId);

        assertThat(loadedState).isEqualTo(originalState);
        assertThat(loadedState.isAiControlled()).isTrue();
        assertThat(loadedState.getCurrentState()).isEqualTo(AIState.IDLE);
        assertThat(loadedState.getEntityType()).isEqualTo("wolf");
    }

    @Test
    void shouldReturnNullForNonExistentEntity() {
        EntityId nonExistentId = EntityId.generate();

        AiEntityState state = persistenceService.loadAiState(nonExistentId);

        assertThat(state).isNull();
        assertThat(persistenceService.hasAiState(nonExistentId)).isFalse();
    }

    @Test
    void shouldTrackAiControlledEntities() {
        EntityId aiEntityId = EntityId.generate();
        EntityId nonAiEntityId = EntityId.generate();
        
        AiEntityState aiState = AiEntityState.createAiControlled(AIState.IDLE, "wolf");
        AiEntityState nonAiState = AiEntityState.createNonAi("wolf");

        persistenceService.saveAiState(aiEntityId, aiState);
        persistenceService.saveAiState(nonAiEntityId, nonAiState);

        assertThat(persistenceService.hasAiState(aiEntityId)).isTrue();
        assertThat(persistenceService.hasAiState(nonAiEntityId)).isTrue();
        
        AiEntityState loadedAiState = persistenceService.loadAiState(aiEntityId);
        AiEntityState loadedNonAiState = persistenceService.loadAiState(nonAiEntityId);
        
        assertThat(loadedAiState.isAiControlled()).isTrue();
        assertThat(loadedNonAiState.isAiControlled()).isFalse();
    }

    @Test
    void shouldRemoveAiState() {
        EntityId entityId = EntityId.generate();
        AiEntityState state = AiEntityState.createAiControlled(AIState.IDLE, "wolf");

        persistenceService.saveAiState(entityId, state);
        assertThat(persistenceService.hasAiState(entityId)).isTrue();

        persistenceService.removeAiState(entityId);
        assertThat(persistenceService.hasAiState(entityId)).isFalse();
        assertThat(persistenceService.loadAiState(entityId)).isNull();
    }

    @Test
    void shouldGetAllAiEntityIds() {
        EntityId entityId1 = EntityId.generate();
        EntityId entityId2 = EntityId.generate();
        AiEntityState state = AiEntityState.createAiControlled(AIState.IDLE, "wolf");

        persistenceService.saveAiState(entityId1, state);
        persistenceService.saveAiState(entityId2, state);

        EntityId[] allIds = persistenceService.getAllAiEntityIds();

        assertThat(allIds).hasSize(2);
        assertThat(allIds).contains(entityId1, entityId2);
    }

    @Test
    void shouldCreateImmutableAiEntityState() {
        AiEntityState original = AiEntityState.createAiControlled(AIState.IDLE, "wolf");
        AiEntityState modified = original.withState(AIState.MOVING);

        assertThat(original.getCurrentState()).isEqualTo(AIState.IDLE);
        assertThat(modified.getCurrentState()).isEqualTo(AIState.MOVING);
        assertThat(original).isNotEqualTo(modified);
    }
}