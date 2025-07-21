package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.EntityResolverService;
import com.aimobs.entity.ai.core.TargetEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

/**
 * Application layer implementation of EntityResolverService.
 * Contains pure business logic for entity resolution.
 * 
 * Following Ben Johnson's standard package layout:
 * - Application layer implements service contracts
 * - Contains business logic but no infrastructure concerns
 */
public class EntityResolver implements EntityResolverService {
    
    @Override
    public Optional<TargetEntity> resolveEntity(String targetType, World world, Vec3d origin, double maxDistance) {
        if (!isValidEntityType(targetType) || world == null || origin == null) {
            return Optional.empty();
        }
        
        Box searchBox = new Box(
            origin.x - maxDistance, origin.y - maxDistance, origin.z - maxDistance,
            origin.x + maxDistance, origin.y + maxDistance, origin.z + maxDistance
        );
        
        switch (targetType.toLowerCase()) {
            case "zombie":
                return findNearestEntity(world, searchBox, origin, net.minecraft.entity.mob.ZombieEntity.class)
                    .map(com.aimobs.entity.ai.core.LivingEntityTarget::new);
            case "skeleton":
                return findNearestEntity(world, searchBox, origin, net.minecraft.entity.mob.SkeletonEntity.class)
                    .map(com.aimobs.entity.ai.core.LivingEntityTarget::new);
            case "spider":
                return findNearestEntity(world, searchBox, origin, net.minecraft.entity.mob.SpiderEntity.class)
                    .map(com.aimobs.entity.ai.core.LivingEntityTarget::new);
            case "creeper":
                return findNearestEntity(world, searchBox, origin, net.minecraft.entity.mob.CreeperEntity.class)
                    .map(com.aimobs.entity.ai.core.LivingEntityTarget::new);
            case "hostile":
            case "enemy":
                return findNearestEntity(world, searchBox, origin, HostileEntity.class)
                    .map(com.aimobs.entity.ai.core.LivingEntityTarget::new);
            default:
                // Try to find any hostile entity as fallback
                return findNearestEntity(world, searchBox, origin, HostileEntity.class)
                    .map(com.aimobs.entity.ai.core.LivingEntityTarget::new);
        }
    }
    
    @Override
    public boolean isValidEntityType(String targetType) {
        if (targetType == null || targetType.trim().isEmpty()) {
            return false;
        }
        
        String lowerType = targetType.toLowerCase();
        return "zombie".equals(lowerType) || 
               "skeleton".equals(lowerType) || 
               "spider".equals(lowerType) || 
               "creeper".equals(lowerType) || 
               "hostile".equals(lowerType) || 
               "enemy".equals(lowerType);
    }
    
    @Override
    public boolean isValidInteractionTarget(TargetEntity entity) {
        if (entity == null || !entity.isAlive()) {
            return false;
        }
        
        // For our wrapper, we can check the underlying Minecraft entity
        if (entity instanceof com.aimobs.entity.ai.core.LivingEntityTarget) {
            LivingEntity minecraftEntity = ((com.aimobs.entity.ai.core.LivingEntityTarget) entity).getLivingEntity();
            
            // Don't target players for safety
            if (minecraftEntity instanceof PlayerEntity) {
                return false;
            }
            
            // Only target hostile entities for combat
            return minecraftEntity instanceof HostileEntity;
        }
        
        // For other implementations, assume valid if alive
        return true;
    }
    
    @Override
    public Optional<com.aimobs.entity.AiControlledWolfEntity> findAiWolfById(com.aimobs.entity.ai.core.EntityId entityId, World world) {
        // This is a temporary implementation - will be moved to infrastructure layer
        // TODO: Move to MinecraftEntityLookupService
        return Optional.empty();
    }
    
    @Override
    public Optional<com.aimobs.entity.AiControlledWolfEntity> findAnyAvailableWolf(World world) {
        // This is a temporary implementation - will be moved to infrastructure layer
        // TODO: Move to MinecraftEntityLookupService
        return Optional.empty();
    }
    
    private <T extends LivingEntity> Optional<T> findNearestEntity(World world, Box searchBox, Vec3d origin, Class<T> entityClass) {
        List<T> entities = world.getEntitiesByClass(entityClass, searchBox, entity -> {
            // Don't target players for safety
            if (entity instanceof PlayerEntity) {
                return false;
            }
            
            // Only target hostile entities for combat
            return entity instanceof HostileEntity && entity.isAlive();
        });
        
        return entities.stream()
            .min((a, b) -> Double.compare(origin.distanceTo(a.getPos()), origin.distanceTo(b.getPos())));
    }
}