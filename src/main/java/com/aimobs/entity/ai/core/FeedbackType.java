package com.aimobs.entity.ai.core;

/**
 * Enumeration of feedback types for AI wolf command execution.
 * Used to categorize different types of visual, audio, and status feedback.
 */
public enum FeedbackType {
    /**
     * Feedback when a command is first received.
     */
    COMMAND_RECEIVED,
    
    /**
     * Feedback when a command is currently executing.
     */
    EXECUTING,
    
    /**
     * Feedback when a command completes successfully.
     */
    SUCCESS,
    
    /**
     * Feedback when a command fails or encounters an error.
     */
    FAILURE,
    
    /**
     * Feedback specific to movement actions.
     */
    MOVEMENT,
    
    /**
     * Feedback specific to combat actions.
     */
    COMBAT,
    
    /**
     * Feedback specific to item collection actions.
     */
    COLLECTION,
    
    /**
     * Feedback specific to defensive/guarding actions.
     */
    DEFENSE
}