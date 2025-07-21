package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.core.TargetEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Concrete implementation of TargetEntity that directly uses Minecraft's LivingEntity.
 * 
 * Part of the application layer - provides concrete implementation using infrastructure.
 * Following Ben Johnson's standard package layout principles.
 * 
 * This extends LivingEntity to provide both our interface and Minecraft compatibility.
 */
public abstract class MinecraftTargetEntity extends LivingEntity implements TargetEntity {
    
    public MinecraftTargetEntity(net.minecraft.entity.EntityType<? extends LivingEntity> entityType, net.minecraft.world.World world) {
        super(entityType, world);
    }
    
    @Override
    public Vec3d getPosition() {
        return this.getPos();
    }
    
    @Override
    public String getEntityId() {
        return this.getUuidAsString();
    }
    
    @Override
    public String getEntityType() {
        return this.getType().toString();
    }
}