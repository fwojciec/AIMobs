package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.core.FeedbackType;
import java.util.UUID;

/**
 * Infrastructure adapter interface for audio feedback in Minecraft.
 * Handles the platform-specific implementation of sound effects.
 */
public interface AudioAdapter {
    /**
     * Plays a sound effect for a specific feedback type.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param type the type of feedback sound to play
     */
    void playSound(UUID wolfId, FeedbackType type);
    
    /**
     * Plays movement-related audio for a wolf.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void playMovementSound(UUID wolfId);
    
    /**
     * Plays combat-related audio for a wolf.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void playCombatSound(UUID wolfId);
    
    /**
     * Plays collection-related audio for a wolf.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void playCollectionSound(UUID wolfId);
    
    /**
     * Calculates volume based on distance between wolf and player.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param playerId the unique identifier of the player
     * @return volume level between 0.0 and 1.0
     */
    float calculateDistanceBasedVolume(UUID wolfId, UUID playerId);
}