package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AICommand;

/**
 * Service contract for entities that can receive and process AI commands.
 * Abstracts command delivery from entity implementation details.
 * 
 * Root interface - defines what the system does (contract only).
 * Dependencies: Core layer only.
 */
public interface CommandReceiver {
    
    /**
     * Gets the unique identifier for this command receiver.
     * Used for routing commands to specific entities.
     * 
     * @return The unique entity identifier
     */
    EntityId getEntityId();
    
    /**
     * Receives and processes an AI command.
     * Implementation handles command queuing and execution.
     * 
     * @param command The command to process
     */
    void receiveCommand(AICommand command);
    
    /**
     * Checks if this receiver is available to process commands.
     * Considers entity state, health, and readiness.
     * 
     * @return true if the receiver can accept commands
     */
    boolean isAvailable();
    
    /**
     * Gets the current command queue size for this receiver.
     * Used for load balancing and monitoring.
     * 
     * @return Number of queued commands
     */
    int getQueuedCommandCount();
}