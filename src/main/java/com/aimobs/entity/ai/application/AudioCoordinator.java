package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.AudioService;
import com.aimobs.entity.ai.core.FeedbackType;
import com.aimobs.entity.ai.infrastructure.AudioAdapter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application service for coordinating audio feedback.
 * Manages audio business logic including volume calculation and sound timing.
 */
public class AudioCoordinator implements AudioService {
    private final AudioAdapter audioAdapter;
    private final Map<UUID, Long> lastSoundTime = new ConcurrentHashMap<>();
    private static final long SOUND_COOLDOWN_MS = 300; // Prevent audio spam
    private boolean enabled = true;
    
    public AudioCoordinator(AudioAdapter audioAdapter) {
        this.audioAdapter = audioAdapter;
    }
    
    @Override
    public void playFeedbackSound(UUID wolfId, FeedbackType type) {
        if (!enabled) {
            return;
        }
        
        if (shouldPlaySound(wolfId)) {
            audioAdapter.playSound(wolfId, type);
            lastSoundTime.put(wolfId, System.currentTimeMillis());
        }
    }
    
    @Override
    public void playMovementSound(UUID wolfId) {
        if (!enabled) {
            return;
        }
        
        // Movement sounds can be more frequent but still limited
        if (shouldPlayMovementSound(wolfId)) {
            audioAdapter.playMovementSound(wolfId);
            lastSoundTime.put(wolfId, System.currentTimeMillis() - (SOUND_COOLDOWN_MS / 2));
        }
    }
    
    @Override
    public void playCombatSound(UUID wolfId) {
        if (!enabled) {
            return;
        }
        
        if (shouldPlaySound(wolfId)) {
            audioAdapter.playCombatSound(wolfId);
            lastSoundTime.put(wolfId, System.currentTimeMillis());
        }
    }
    
    @Override
    public void playCollectionSound(UUID wolfId) {
        if (!enabled) {
            return;
        }
        
        if (shouldPlaySound(wolfId)) {
            audioAdapter.playCollectionSound(wolfId);
            lastSoundTime.put(wolfId, System.currentTimeMillis());
        }
    }
    
    @Override
    public float calculateVolume(UUID wolfId, UUID playerId) {
        return audioAdapter.calculateDistanceBasedVolume(wolfId, playerId);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Determines if enough time has passed to play another sound for this wolf.
     * Prevents audio spam and overlapping sounds.
     */
    private boolean shouldPlaySound(UUID wolfId) {
        Long lastTime = lastSoundTime.get(wolfId);
        if (lastTime == null) {
            return true;
        }
        return System.currentTimeMillis() - lastTime >= SOUND_COOLDOWN_MS;
    }
    
    /**
     * Special timing check for movement sounds which can be more frequent.
     */
    private boolean shouldPlayMovementSound(UUID wolfId) {
        Long lastTime = lastSoundTime.get(wolfId);
        if (lastTime == null) {
            return true;
        }
        return System.currentTimeMillis() - lastTime >= (SOUND_COOLDOWN_MS / 2);
    }
}