package com.aimobs.entity.ai.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Simple implementation of TargetEntity that wraps any LivingEntity.
 * 
 * Part of the core layer - provides concrete implementation.
 * Following dependency injection principles - we inject the LivingEntity.
 * 
 * This allows us to use any existing Minecraft entity as a target while
 * maintaining our clean interface boundaries.
 */
public class LivingEntityTarget implements TargetEntity {
    
    private final LivingEntity entity;
    
    public LivingEntityTarget(LivingEntity entity) {
        this.entity = entity;
    }
    
    @Override
    public boolean isAlive() {
        return entity != null && entity.isAlive();
    }
    
    @Override
    public Vec3d getPosition() {
        return entity != null ? entity.getPos() : new Vec3d(0, 0, 0);
    }
    
    @Override
    public String getEntityId() {
        return entity != null ? entity.getUuidAsString() : "unknown";
    }
    
    @Override
    public String getEntityType() {
        return entity != null ? entity.getType().toString() : "unknown";
    }
    
    /**
     * Gets the underlying LivingEntity for infrastructure operations.
     * This provides the seam - infrastructure can get the real entity when needed.
     */
    public LivingEntity getLivingEntity() {
        return entity;
    }
}