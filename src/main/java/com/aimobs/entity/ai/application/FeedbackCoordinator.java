package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.AudioService;
import com.aimobs.entity.ai.FeedbackService;
import com.aimobs.entity.ai.ParticleService;
import com.aimobs.entity.ai.StatusDisplayService;
import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.entity.ai.core.FeedbackEvent;
import com.aimobs.entity.ai.core.FeedbackType;
import com.aimobs.entity.ai.core.StatusIcon;
import com.aimobs.network.MessageService;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * Application service that coordinates feedback across all feedback systems.
 * Implements business logic for determining appropriate feedback responses.
 */
public class FeedbackCoordinator implements FeedbackService {
    private final ParticleService particleService;
    private final AudioService audioService;
    private final StatusDisplayService statusDisplayService;
    private final MessageService messageService;
    
    public FeedbackCoordinator(
            ParticleService particleService,
            AudioService audioService,
            StatusDisplayService statusDisplayService,
            MessageService messageService) {
        this.particleService = particleService;
        this.audioService = audioService;
        this.statusDisplayService = statusDisplayService;
        this.messageService = messageService;
    }
    
    @Override
    public void onCommandReceived(UUID wolfId, AICommand command) {
        FeedbackEvent event = FeedbackEvent.commandReceived(wolfId, getCommandAction(command));
        
        // Coordinate all feedback systems
        particleService.spawnCommandEffect(wolfId, FeedbackType.COMMAND_RECEIVED);
        audioService.playFeedbackSound(wolfId, FeedbackType.COMMAND_RECEIVED);
        statusDisplayService.updateStatus(wolfId, "Processing...", StatusIcon.PROCESSING);
        
        // Send message feedback if message service is available
        if (messageService != null) {
            sendInfoMessage(event.message());
        }
    }
    
    @Override
    public void onCommandExecuting(UUID wolfId, String action) {
        FeedbackEvent event = FeedbackEvent.commandExecuting(wolfId, action);
        
        // Determine appropriate feedback type and icon based on action
        FeedbackType feedbackType = determineFeedbackType(action);
        StatusIcon statusIcon = determineStatusIcon(action);
        
        particleService.spawnCommandEffect(wolfId, feedbackType);
        statusDisplayService.updateStatus(wolfId, event.message(), statusIcon);
        
        // Action-specific feedback
        triggerActionSpecificFeedback(wolfId, action);
    }
    
    @Override
    public void onCommandCompleted(UUID wolfId, String action) {
        FeedbackEvent event = FeedbackEvent.commandCompleted(wolfId, action);
        
        particleService.spawnCommandEffect(wolfId, FeedbackType.SUCCESS);
        audioService.playFeedbackSound(wolfId, FeedbackType.SUCCESS);
        statusDisplayService.updateStatus(wolfId, "Idle", StatusIcon.IDLE);
        
        if (messageService != null) {
            sendSuccessMessage(event.message());
        }
    }
    
    @Override
    public void onCommandFailed(UUID wolfId, String action, String reason) {
        FeedbackEvent event = FeedbackEvent.commandFailed(wolfId, action, reason);
        
        particleService.spawnCommandEffect(wolfId, FeedbackType.FAILURE);
        audioService.playFeedbackSound(wolfId, FeedbackType.FAILURE);
        statusDisplayService.updateStatus(wolfId, "Error: " + reason, StatusIcon.ERROR);
        
        if (messageService != null) {
            sendErrorMessage(event.message());
        }
    }
    
    @Override
    public void onMovementStarted(UUID wolfId, double targetX, double targetY, double targetZ) {
        BlockPos target = new BlockPos((int)targetX, (int)targetY, (int)targetZ);
        
        particleService.spawnTargetIndicator(target);
        particleService.spawnMovementTrail(wolfId);
        audioService.playMovementSound(wolfId);
        statusDisplayService.updateStatus(wolfId, "Moving to target", StatusIcon.MOVEMENT);
    }
    
    @Override
    public void onAttackStarted(UUID wolfId, UUID targetEntityId) {
        particleService.spawnCombatEffect(wolfId);
        audioService.playCombatSound(wolfId);
        statusDisplayService.updateStatus(wolfId, "Attacking target", StatusIcon.COMBAT);
    }
    
    @Override
    public void onCollectionStarted(UUID wolfId, String itemType) {
        particleService.spawnCollectionEffect(wolfId);
        audioService.playCollectionSound(wolfId);
        statusDisplayService.updateStatus(wolfId, "Collecting " + itemType, StatusIcon.COLLECTION);
    }
    
    /**
     * Determines the appropriate feedback type based on the action.
     */
    private FeedbackType determineFeedbackType(String action) {
        return switch (action.toLowerCase()) {
            case "move", "moveto", "comehere", "follow" -> FeedbackType.MOVEMENT;
            case "attack", "attacktarget" -> FeedbackType.COMBAT;
            case "collect", "collectitems" -> FeedbackType.COLLECTION;
            case "defend", "defendarea" -> FeedbackType.DEFENSE;
            default -> FeedbackType.EXECUTING;
        };
    }
    
    /**
     * Determines the appropriate status icon based on the action.
     */
    private StatusIcon determineStatusIcon(String action) {
        return switch (action.toLowerCase()) {
            case "move", "moveto", "comehere", "follow" -> StatusIcon.MOVEMENT;
            case "attack", "attacktarget" -> StatusIcon.COMBAT;
            case "collect", "collectitems" -> StatusIcon.COLLECTION;
            case "defend", "defendarea" -> StatusIcon.DEFENSE;
            default -> StatusIcon.PROCESSING;
        };
    }
    
    /**
     * Triggers action-specific feedback effects.
     */
    private void triggerActionSpecificFeedback(UUID wolfId, String action) {
        switch (action.toLowerCase()) {
            case "move", "moveto", "comehere", "follow" -> {
                particleService.spawnMovementTrail(wolfId);
                audioService.playMovementSound(wolfId);
            }
            case "attack", "attacktarget" -> {
                particleService.spawnCombatEffect(wolfId);
                audioService.playCombatSound(wolfId);
            }
            case "collect", "collectitems" -> {
                particleService.spawnCollectionEffect(wolfId);
                audioService.playCollectionSound(wolfId);
            }
        }
    }
    
    /**
     * Extracts action name from AI command for feedback purposes.
     */
    private String getCommandAction(AICommand command) {
        // Use the command class name as a fallback
        String className = command.getClass().getSimpleName();
        return className.replace("Command", "").toLowerCase();
    }
    
    /**
     * Sends informational message through the message service.
     */
    private void sendInfoMessage(String message) {
        // Implementation depends on MessageService interface
        // This would typically format and send the message
    }
    
    /**
     * Sends success message through the message service.
     */
    private void sendSuccessMessage(String message) {
        // Implementation depends on MessageService interface
    }
    
    /**
     * Sends error message through the message service.
     */
    private void sendErrorMessage(String message) {
        // Implementation depends on MessageService interface
    }
}