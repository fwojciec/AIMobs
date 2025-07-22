package com.aimobs.entity.ai.core;

import java.util.UUID;

/**
 * Immutable record representing a feedback event in the AI wolf system.
 * Contains all necessary information for generating appropriate feedback.
 */
public record FeedbackEvent(
    UUID wolfId,
    FeedbackType type,
    String message,
    long timestamp
) {
    /**
     * Creates a new feedback event with the current timestamp.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param type the type of feedback event
     * @param message the associated message
     */
    public FeedbackEvent(UUID wolfId, FeedbackType type, String message) {
        this(wolfId, type, message, System.currentTimeMillis());
    }
    
    /**
     * Creates a feedback event for command reception.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param commandAction the action of the received command
     * @return a new feedback event
     */
    public static FeedbackEvent commandReceived(UUID wolfId, String commandAction) {
        return new FeedbackEvent(wolfId, FeedbackType.COMMAND_RECEIVED, 
            "Command received: " + commandAction);
    }
    
    /**
     * Creates a feedback event for command execution.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param action the action being executed
     * @return a new feedback event
     */
    public static FeedbackEvent commandExecuting(UUID wolfId, String action) {
        return new FeedbackEvent(wolfId, FeedbackType.EXECUTING, 
            "Executing: " + action);
    }
    
    /**
     * Creates a feedback event for successful command completion.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param action the action that was completed
     * @return a new feedback event
     */
    public static FeedbackEvent commandCompleted(UUID wolfId, String action) {
        return new FeedbackEvent(wolfId, FeedbackType.SUCCESS, 
            "Completed: " + action);
    }
    
    /**
     * Creates a feedback event for command failure.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param action the action that failed
     * @param reason the reason for failure
     * @return a new feedback event
     */
    public static FeedbackEvent commandFailed(UUID wolfId, String action, String reason) {
        return new FeedbackEvent(wolfId, FeedbackType.FAILURE, 
            "Failed " + action + ": " + reason);
    }
}