package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.AiPersistenceService;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AiEntityState;
import com.aimobs.entity.ai.core.AIState;
import com.aimobs.entity.AiControlledWolfEntity;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Infrastructure adapter implementing AI persistence through Minecraft's entity system.
 * Stores AI state by mapping it to actual entities in the world.
 * 
 * This is a thin adapter - delegates to Minecraft APIs but keeps business logic minimal.
 * Infrastructure layer - can depend on all other layers.
 */
public class MinecraftAiPersistenceAdapter implements AiPersistenceService {
    
    private final ServerWorld world;

    public MinecraftAiPersistenceAdapter(ServerWorld world) {
        this.world = world;
    }

    @Override
    public void saveAiState(EntityId entityId, AiEntityState state) {
        // Find the entity in the world
        AiControlledWolfEntity entity = findAiWolfByEntityId(entityId);
        if (entity != null) {
            // State is automatically saved to NBT when entity is saved
            // The entity's writeCustomDataToNbt method handles the persistence
            // Minecraft handles entity saving automatically when the world saves
        }
    }

    @Override
    public AiEntityState loadAiState(EntityId entityId) {
        AiControlledWolfEntity entity = findAiWolfByEntityId(entityId);
        if (entity != null) {
            // Extract current state from the living entity
            return AiEntityState.createAiControlled(
                entity.getCurrentState(),
                "ai_controlled_wolf"
            );
        }
        return null;
    }

    @Override
    public boolean hasAiState(EntityId entityId) {
        return findAiWolfByEntityId(entityId) != null;
    }

    @Override
    public void removeAiState(EntityId entityId) {
        AiControlledWolfEntity entity = findAiWolfByEntityId(entityId);
        if (entity != null) {
            // Mark entity for removal
            entity.discard();
        }
    }

    @Override
    public EntityId[] getAllAiEntityIds() {
        List<EntityId> entityIds = new ArrayList<>();
        
        // Search for all AI-controlled wolves in the world
        List<AiControlledWolfEntity> aiWolves = world.getEntitiesByClass(
            AiControlledWolfEntity.class,
            new Box(-30000000, -512, -30000000, 30000000, 512, 30000000), // Large search box
            entity -> entity.isAlive()
        );
        
        for (AiControlledWolfEntity wolf : aiWolves) {
            entityIds.add(wolf.getAiEntityId());
        }
        
        return entityIds.toArray(new EntityId[0]);
    }

    /**
     * Finds an AI-controlled wolf entity by its EntityId.
     * This is the core infrastructure logic that maps our domain EntityId
     * to actual Minecraft entities in the world.
     */
    private AiControlledWolfEntity findAiWolfByEntityId(EntityId entityId) {
        // Search for AI wolves in a large area (effectively the entire world)
        List<AiControlledWolfEntity> aiWolves = world.getEntitiesByClass(
            AiControlledWolfEntity.class,
            new Box(-30000000, -512, -30000000, 30000000, 512, 30000000),
            entity -> entity.isAlive() && entity.getAiEntityId().equals(entityId)
        );
        
        return aiWolves.isEmpty() ? null : aiWolves.get(0);
    }
}