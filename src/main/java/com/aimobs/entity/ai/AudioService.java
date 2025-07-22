package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.FeedbackType;
import java.util.UUID;

/**
 * Service interface for managing audio feedback in the AI wolf system.
 * Provides sound effects for various command states and actions.
 */
public interface AudioService {
    /**
     * Plays a feedback sound for a specific wolf and feedback type.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param type the type of feedback sound to play
     */
    void playFeedbackSound(UUID wolfId, FeedbackType type);
    
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
     * Calculates appropriate volume based on distance from player.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param playerId the unique identifier of the player
     * @return volume level between 0.0 and 1.0
     */
    float calculateVolume(UUID wolfId, UUID playerId);
    
    /**
     * Enables or disables audio feedback.
     * 
     * @param enabled whether audio should be enabled
     */
    void setEnabled(boolean enabled);
}