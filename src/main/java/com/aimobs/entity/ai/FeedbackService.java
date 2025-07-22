package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.AICommand;
import java.util.UUID;

/**
 * Service interface for providing visual, audio, and text feedback for AI wolf command execution.
 * Follows interface-first development pattern for testability and flexibility.
 */
public interface FeedbackService {
    /**
     * Triggered when a command is received but not yet executed.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param command the command that was received
     */
    void onCommandReceived(UUID wolfId, AICommand command);
    
    /**
     * Triggered when a command begins execution.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param action the action being executed (e.g., "move", "attack")
     */
    void onCommandExecuting(UUID wolfId, String action);
    
    /**
     * Triggered when a command completes successfully.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param action the action that was completed
     */
    void onCommandCompleted(UUID wolfId, String action);
    
    /**
     * Triggered when a command fails to execute.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param action the action that failed
     * @param reason the reason for failure
     */
    void onCommandFailed(UUID wolfId, String action, String reason);
    
    /**
     * Triggered when movement starts to a specific target.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param targetX the target X coordinate
     * @param targetY the target Y coordinate
     * @param targetZ the target Z coordinate
     */
    void onMovementStarted(UUID wolfId, double targetX, double targetY, double targetZ);
    
    /**
     * Triggered when an attack action begins.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param targetEntityId the target entity being attacked
     */
    void onAttackStarted(UUID wolfId, UUID targetEntityId);
    
    /**
     * Triggered when item collection begins.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param itemType the type of item being collected
     */
    void onCollectionStarted(UUID wolfId, String itemType);
}