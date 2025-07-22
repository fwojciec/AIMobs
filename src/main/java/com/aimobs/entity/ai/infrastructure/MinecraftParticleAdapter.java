package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.AiControlledWolfEntity;
import com.aimobs.entity.ai.EntityLookupService;
import com.aimobs.entity.ai.core.FeedbackType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

/**
 * Minecraft-specific implementation of particle effects for AI wolf feedback.
 * Handles spawning and managing particle effects using Minecraft's particle system.
 */
public class MinecraftParticleAdapter implements ParticleAdapter {
    private final EntityLookupService entityLookupService;
    
    public MinecraftParticleAdapter(EntityLookupService entityLookupService) {
        this.entityLookupService = entityLookupService;
    }
    
    @Override
    public void spawnParticleEffect(UUID wolfId, FeedbackType type) {
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf == null || !(wolf.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        
        Vec3d pos = wolf.getPos();
        double x = pos.x;
        double y = pos.y + 1.0; // Above the wolf
        double z = pos.z;
        
        switch (type) {
            case COMMAND_RECEIVED -> spawnEnchantParticles(serverWorld, x, y, z);
            case EXECUTING -> spawnCloudParticles(serverWorld, x, y, z);
            case SUCCESS -> spawnHappyVillagerParticles(serverWorld, x, y, z);
            case FAILURE -> spawnAngryVillagerParticles(serverWorld, x, y, z);
            case MOVEMENT -> spawnEndRodParticles(serverWorld, x, y, z);
            case COMBAT -> spawnSweepAttackParticles(serverWorld, x, y, z);
            case COLLECTION -> spawnItemPickupParticles(serverWorld, x, y, z);
            case DEFENSE -> spawnTotemParticles(serverWorld, x, y, z);
        }
    }
    
    @Override
    public void spawnTargetParticles(BlockPos target) {
        // Need to get the world context - this would typically be passed in or obtained from context
        // For now, we'll implement this when we have the world context available
        // This method would spawn END_ROD particles at the target location
    }
    
    @Override
    public void spawnMovementParticles(UUID wolfId) {
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf == null || !(wolf.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        
        Vec3d pos = wolf.getPos();
        // Spawn trail particles behind the wolf
        serverWorld.spawnParticles(
            ParticleTypes.CLOUD,
            pos.x, pos.y + 0.1, pos.z,
            3, // particle count
            0.3, 0.1, 0.3, // spread
            0.02 // speed
        );
    }
    
    @Override
    public void spawnCombatParticles(UUID wolfId) {
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf == null || !(wolf.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        
        Vec3d pos = wolf.getPos();
        spawnSweepAttackParticles(serverWorld, pos.x, pos.y + 0.5, pos.z);
    }
    
    @Override
    public void spawnCollectionParticles(UUID wolfId) {
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf == null || !(wolf.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        
        Vec3d pos = wolf.getPos();
        spawnItemPickupParticles(serverWorld, pos.x, pos.y + 0.5, pos.z);
    }
    
    @Override
    public void clearParticleEffects(UUID wolfId) {
        // Minecraft doesn't have a direct way to clear specific particles
        // This could be implemented by tracking active particle effects and managing them
        // For now, this is a no-op as particles naturally fade
    }
    
    // Helper methods for specific particle types
    
    private void spawnEnchantParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
            ParticleTypes.ENCHANT,
            x, y, z,
            8, // particle count
            0.5, 0.5, 0.5, // spread
            0.1 // speed
        );
    }
    
    private void spawnCloudParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
            ParticleTypes.CLOUD,
            x, y, z,
            5, // particle count
            0.3, 0.3, 0.3, // spread
            0.05 // speed
        );
    }
    
    private void spawnHappyVillagerParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
            ParticleTypes.HAPPY_VILLAGER,
            x, y, z,
            6, // particle count
            0.4, 0.4, 0.4, // spread
            0.1 // speed
        );
    }
    
    private void spawnAngryVillagerParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
            ParticleTypes.ANGRY_VILLAGER,
            x, y, z,
            4, // particle count
            0.3, 0.3, 0.3, // spread
            0.1 // speed
        );
    }
    
    private void spawnEndRodParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
            ParticleTypes.END_ROD,
            x, y, z,
            3, // particle count
            0.2, 0.2, 0.2, // spread
            0.08 // speed
        );
    }
    
    private void spawnSweepAttackParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
            ParticleTypes.SWEEP_ATTACK,
            x, y, z,
            1, // particle count
            0.0, 0.0, 0.0, // spread
            0.0 // speed
        );
    }
    
    private void spawnItemPickupParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
            ParticleTypes.COMPOSTER,
            x, y, z,
            5, // particle count
            0.3, 0.3, 0.3, // spread
            0.1 // speed
        );
    }
    
    private void spawnTotemParticles(ServerWorld world, double x, double y, double z) {
        world.spawnParticles(
            ParticleTypes.TOTEM_OF_UNDYING,
            x, y, z,
            4, // particle count
            0.4, 0.4, 0.4, // spread
            0.1 // speed
        );
    }
}