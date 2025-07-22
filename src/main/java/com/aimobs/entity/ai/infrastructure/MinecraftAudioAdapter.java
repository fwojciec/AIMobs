package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.AiControlledWolfEntity;
import com.aimobs.entity.ai.EntityLookupService;
import com.aimobs.entity.ai.core.FeedbackType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

/**
 * Minecraft-specific implementation of audio feedback for AI wolves.
 * Handles sound playback using Minecraft's sound system.
 */
public class MinecraftAudioAdapter implements AudioAdapter {
    private final EntityLookupService entityLookupService;
    private static final float MAX_VOLUME = 1.0f;
    private static final float MAX_DISTANCE = 16.0f; // Distance at which sounds become inaudible
    
    public MinecraftAudioAdapter(EntityLookupService entityLookupService) {
        this.entityLookupService = entityLookupService;
    }
    
    @Override
    public void playSound(UUID wolfId, FeedbackType type) {
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf == null || !(wolf.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        
        Vec3d pos = wolf.getPos();
        
        switch (type) {
            case COMMAND_RECEIVED -> playCommandReceivedSound(serverWorld, pos);
            case EXECUTING -> playExecutingSound(serverWorld, pos);
            case SUCCESS -> playSuccessSound(serverWorld, pos);
            case FAILURE -> playFailureSound(serverWorld, pos);
            case MOVEMENT -> playMovementSound(wolfId);
            case COMBAT -> playCombatSound(wolfId);
            case COLLECTION -> playCollectionSound(wolfId);
            case DEFENSE -> playDefenseSound(serverWorld, pos);
        }
    }
    
    @Override
    public void playMovementSound(UUID wolfId) {
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf == null || !(wolf.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        
        Vec3d pos = wolf.getPos();
        serverWorld.playSound(
            null, // null means play to all players
            pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_WOLF_STEP,
            SoundCategory.NEUTRAL,
            0.4f, // volume
            1.2f  // pitch - slightly higher for AI wolf
        );
    }
    
    @Override
    public void playCombatSound(UUID wolfId) {
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf == null || !(wolf.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        
        Vec3d pos = wolf.getPos();
        serverWorld.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_WOLF_GROWL,
            SoundCategory.NEUTRAL,
            0.6f, // volume
            1.0f  // pitch
        );
    }
    
    @Override
    public void playCollectionSound(UUID wolfId) {
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf == null || !(wolf.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        
        Vec3d pos = wolf.getPos();
        serverWorld.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_ITEM_PICKUP,
            SoundCategory.NEUTRAL,
            0.5f, // volume
            1.1f  // pitch
        );
    }
    
    @Override
    public float calculateDistanceBasedVolume(UUID wolfId, UUID playerId) {
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf == null) {
            return 0.0f;
        }
        
        // Find the player entity - this is simplified, in practice you'd need a player lookup service
        PlayerEntity player = wolf.getWorld().getPlayerByUuid(playerId);
        if (player == null) {
            return MAX_VOLUME; // Default volume if player not found
        }
        
        double distance = wolf.getPos().distanceTo(player.getPos());
        
        if (distance >= MAX_DISTANCE) {
            return 0.0f;
        }
        
        // Linear falloff: volume = 1.0 at distance 0, volume = 0.0 at MAX_DISTANCE
        float volume = 1.0f - (float) (distance / MAX_DISTANCE);
        return Math.max(0.0f, Math.min(MAX_VOLUME, volume));
    }
    
    // Helper methods for specific sound types
    
    private void playCommandReceivedSound(ServerWorld world, Vec3d pos) {
        world.playSound(
            null, // player (null = everyone can hear)
            pos.x, pos.y, pos.z,
            SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(),
            SoundCategory.NEUTRAL,
            0.3f, // volume
            1.5f  // pitch - higher pitch for command received
        );
    }
    
    private void playExecutingSound(ServerWorld world, Vec3d pos) {
        world.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
            SoundCategory.NEUTRAL,
            0.2f, // volume
            1.0f  // pitch
        );
    }
    
    private void playSuccessSound(ServerWorld world, Vec3d pos) {
        world.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
            SoundCategory.NEUTRAL,
            0.4f, // volume
            1.2f  // pitch
        );
    }
    
    private void playFailureSound(ServerWorld world, Vec3d pos) {
        world.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(),
            SoundCategory.NEUTRAL,
            0.5f, // volume
            0.8f  // pitch - lower pitch for failure
        );
    }
    
    private void playDefenseSound(ServerWorld world, Vec3d pos) {
        world.playSound(
            null,
            pos.x, pos.y, pos.z,
            SoundEvents.ENTITY_WOLF_HOWL,
            SoundCategory.NEUTRAL,
            0.7f, // volume
            1.0f  // pitch
        );
    }
}