package com.aimobs.test;

import com.aimobs.entity.ai.EntityResolverService;
import com.aimobs.entity.ai.core.TargetEntity;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.AiControlledWolfEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Fake implementation of EntityResolverService for testing.
 * Follows the fake object pattern used throughout the test suite.
 */
public class FakeEntityResolverService implements EntityResolverService {
    
    private boolean shouldReturnEntity = true;
    private String lastResolvedType;
    private Vec3d lastOrigin;
    private double lastMaxDistance;
    
    public FakeEntityResolverService() {
        // Fake implementation - no actual entities needed
    }
    
    @Override
    public Optional<TargetEntity> resolveEntity(String targetType, World world, Vec3d origin, double maxDistance) {
        lastResolvedType = targetType;
        lastOrigin = origin;
        lastMaxDistance = maxDistance;
        
        if (shouldReturnEntity && isValidEntityType(targetType)) {
            // Return a simple fake entity for testing
            return Optional.of(new FakeTargetEntity(targetType));
        }
        
        return Optional.empty();
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
        return entity != null && entity.isAlive();
    }
    
    @Override
    public Optional<AiControlledWolfEntity> findAiWolfById(EntityId entityId, World world) {
        // Fake implementation for testing - just return empty
        return Optional.empty();
    }
    
    @Override
    public Optional<AiControlledWolfEntity> findAnyAvailableWolf(World world) {
        // Fake implementation for testing - just return empty
        return Optional.empty();
    }
    
    // Test control methods
    public void setShouldReturnEntity(boolean shouldReturnEntity) {
        this.shouldReturnEntity = shouldReturnEntity;
    }
    
    public String getLastResolvedType() {
        return lastResolvedType;
    }
    
    public Vec3d getLastOrigin() {
        return lastOrigin;
    }
    
    public double getLastMaxDistance() {
        return lastMaxDistance;
    }
    
    public void reset() {
        shouldReturnEntity = true;
        lastResolvedType = null;
        lastOrigin = null;
        lastMaxDistance = 0;
    }
    
    /**
     * Simple test implementation of TargetEntity for testing.
     */
    private static class FakeTargetEntity implements TargetEntity {
        private final String entityType;
        
        public FakeTargetEntity(String entityType) {
            this.entityType = entityType;
        }
        
        @Override
        public boolean isAlive() {
            return true;
        }
        
        @Override
        public Vec3d getPosition() {
            return new Vec3d(0, 0, 0);
        }
        
        @Override
        public String getEntityId() {
            return "test-" + entityType;
        }
        
        @Override
        public String getEntityType() {
            return entityType;
        }
    }
}