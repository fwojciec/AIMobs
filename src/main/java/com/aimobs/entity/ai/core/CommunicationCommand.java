package com.aimobs.entity.ai.core;

/**
 * Command for processing communication messages and generating responses.
 * 
 * Part of the core layer - pure domain object with no dependencies.
 * Following Ben Johnson's standard package layout principles.
 */
public class CommunicationCommand implements InteractionCommand {
    
    private final String message;
    private final int priority;
    private boolean isComplete = false;
    private boolean isCancelled = false;
    private String response;
    
    public CommunicationCommand(String message) {
        this(message, InteractionType.COMMUNICATE.getDefaultPriority());
    }
    
    public CommunicationCommand(String message, int priority) {
        this.message = message;
        this.priority = priority;
    }
    
    @Override
    public void execute() {
        if (isCancelled) {
            isComplete = true;
            return;
        }
        // Execution logic will be handled by the application layer
        // This command completes immediately after generating response
        isComplete = true;
    }
    
    @Override
    public boolean isComplete() {
        return isComplete || isCancelled;
    }
    
    @Override
    public void cancel() {
        isCancelled = true;
        isComplete = true;
    }
    
    @Override
    public InteractionType getInteractionType() {
        return InteractionType.COMMUNICATE;
    }
    
    @Override
    public boolean requiresPositioning() {
        return false;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    /**
     * @return The communication message to process
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * @return The generated response, or null if not yet processed
     */
    public String getResponse() {
        return response;
    }
    
    /**
     * Sets the response for this communication command.
     * 
     * @param response The response to set
     */
    public void setResponse(String response) {
        this.response = response;
    }
    
    /**
     * @return True if the command was cancelled
     */
    public boolean isCancelled() {
        return isCancelled;
    }
}