package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.ParticleService;
import com.aimobs.entity.ai.core.FeedbackType;
import com.aimobs.entity.ai.infrastructure.ParticleAdapter;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application service for coordinating particle effects.
 * Manages particle effect business logic and delegates to infrastructure adapter.
 */
public class ParticleCoordinator implements ParticleService {
    private final ParticleAdapter particleAdapter;
    private final Map<UUID, Long> lastEffectTime = new ConcurrentHashMap<>();
    private static final long EFFECT_COOLDOWN_MS = 500; // Prevent particle spam
    
    public ParticleCoordinator(ParticleAdapter particleAdapter) {
        this.particleAdapter = particleAdapter;
    }
    
    @Override
    public void spawnCommandEffect(UUID wolfId, FeedbackType type) {
        if (shouldSpawnEffect(wolfId)) {
            particleAdapter.spawnParticleEffect(wolfId, type);
            lastEffectTime.put(wolfId, System.currentTimeMillis());
        }
    }
    
    @Override
    public void spawnTargetIndicator(BlockPos target) {
        particleAdapter.spawnTargetParticles(target);
    }
    
    @Override
    public void spawnMovementTrail(UUID wolfId) {
        // Movement trail can be more frequent than other effects
        particleAdapter.spawnMovementParticles(wolfId);
    }
    
    @Override
    public void spawnCombatEffect(UUID wolfId) {
        if (shouldSpawnEffect(wolfId)) {
            particleAdapter.spawnCombatParticles(wolfId);
            lastEffectTime.put(wolfId, System.currentTimeMillis());
        }
    }
    
    @Override
    public void spawnCollectionEffect(UUID wolfId) {
        if (shouldSpawnEffect(wolfId)) {
            particleAdapter.spawnCollectionParticles(wolfId);
            lastEffectTime.put(wolfId, System.currentTimeMillis());
        }
    }
    
    @Override
    public void clearEffects(UUID wolfId) {
        particleAdapter.clearParticleEffects(wolfId);
        lastEffectTime.remove(wolfId);
    }
    
    /**
     * Determines if enough time has passed to spawn another effect for this wolf.
     * Prevents particle spam and performance issues.
     */
    private boolean shouldSpawnEffect(UUID wolfId) {
        Long lastTime = lastEffectTime.get(wolfId);
        if (lastTime == null) {
            return true;
        }
        return System.currentTimeMillis() - lastTime >= EFFECT_COOLDOWN_MS;
    }
}