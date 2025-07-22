package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.EntityLookupService;
import com.aimobs.entity.ai.CommandReceiver;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.AiControlledWolfEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Infrastructure implementation of EntityLookupService.
 * Handles Minecraft-specific entity finding and lookup.
 * 
 * Infrastructure layer - can depend on all other layers.
 * Contains platform-specific implementation details.
 */
public class MinecraftEntityLookupService implements EntityLookupService {
    
    private final ServerWorld world;
    
    public MinecraftEntityLookupService(ServerWorld world) {
        this.world = world;
    }
    
    @Override
    public Optional<CommandReceiver> findEntityById(EntityId entityId) {
        if (entityId == null || world == null) {
            return Optional.empty();
        }
        
        // Search through all entities in the world
        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof AiControlledWolfEntity wolf) {
                if (wolf.getAiEntityId().equals(entityId)) {
                    return Optional.of(wolf);
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<CommandReceiver> getAllAvailableEntities() {
        List<CommandReceiver> availableEntities = new ArrayList<>();
        
        if (world == null) {
            return availableEntities;
        }
        
        // Find all AI-controlled wolves that are available
        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof AiControlledWolfEntity wolf) {
                if (wolf.isAvailable()) {
                    availableEntities.add(wolf);
                }
            }
        }
        
        return availableEntities;
    }
    
    @Override
    public Optional<CommandReceiver> findAnyAvailable() {
        if (world == null) {
            return Optional.empty();
        }
        
        // Find first available AI wolf in the world
        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof AiControlledWolfEntity wolf) {
                if (wolf.isAvailable()) {
                    return Optional.of(wolf);
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public int getAvailableEntityCount() {
        if (world == null) {
            return 0;
        }
        
        int count = 0;
        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof AiControlledWolfEntity wolf) {
                if (wolf.isAvailable()) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    @Override
    public AiControlledWolfEntity findWolfById(UUID wolfId) {
        if (wolfId == null || world == null) {
            return null;
        }
        
        // Search through all entities for the specific wolf UUID
        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof AiControlledWolfEntity wolf) {
                if (wolf.getUuid().equals(wolfId)) {
                    return wolf;
                }
            }
        }
        
        return null;
    }
}