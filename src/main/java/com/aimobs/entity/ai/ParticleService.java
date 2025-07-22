package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.FeedbackType;
import net.minecraft.util.math.BlockPos;
import java.util.UUID;

/**
 * Service interface for managing particle effects in the feedback system.
 * Handles visual feedback through particle spawning and effects.
 */
public interface ParticleService {
    /**
     * Spawns command-related particle effects around a wolf entity.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param type the type of feedback to display
     */
    void spawnCommandEffect(UUID wolfId, FeedbackType type);
    
    /**
     * Spawns particle indicators at a target location.
     * 
     * @param target the target block position
     */
    void spawnTargetIndicator(BlockPos target);
    
    /**
     * Spawns movement trail particles behind a moving wolf.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void spawnMovementTrail(UUID wolfId);
    
    /**
     * Spawns particles indicating the start of combat.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void spawnCombatEffect(UUID wolfId);
    
    /**
     * Spawns particles indicating item collection activity.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void spawnCollectionEffect(UUID wolfId);
    
    /**
     * Clears all particle effects for a specific wolf.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void clearEffects(UUID wolfId);
}