package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.core.FeedbackType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Infrastructure adapter for spawning particle effects in Minecraft.
 * Handles the platform-specific implementation of particle effects.
 */
public interface ParticleAdapter {
    /**
     * Spawns particle effects around a wolf entity based on feedback type.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param type the type of feedback to display
     */
    void spawnParticleEffect(UUID wolfId, FeedbackType type);
    
    /**
     * Spawns target indicator particles at a specific location.
     * 
     * @param target the target block position
     */
    void spawnTargetParticles(BlockPos target);
    
    /**
     * Spawns movement trail particles behind a wolf.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void spawnMovementParticles(UUID wolfId);
    
    /**
     * Spawns combat-related particle effects around a wolf.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void spawnCombatParticles(UUID wolfId);
    
    /**
     * Spawns collection-related particle effects around a wolf.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void spawnCollectionParticles(UUID wolfId);
    
    /**
     * Clears all particle effects for a specific wolf.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void clearParticleEffects(UUID wolfId);
}