package com.aimobs.network;

import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.network.core.NetworkMessage;

/**
 * Root interface defining message parsing and command queuing contract.
 * Following Standard Package Layout - this is a service contract.
 */
public interface MessageService {
    
    /**
     * Parse raw JSON message into structured network message.
     * @param rawMessage Raw JSON string
     * @return Parsed network message, or null if invalid
     */
    NetworkMessage parseMessage(String rawMessage);
    
    /**
     * Validate message structure and content.
     * @param message Message to validate
     * @return true if message is valid, false otherwise
     */
    boolean validateMessage(NetworkMessage message);
    
    /**
     * Convert network message to AI command for execution.
     * @param message Network message
     * @return AI command ready for execution, or null if conversion fails
     */
    AICommand convertToCommand(NetworkMessage message);
    
    /**
     * Queue command for execution by entity system.
     * @param command Command to queue
     */
    void queueCommand(AICommand command);
    
    /**
     * Get count of queued commands.
     * @return Number of commands in queue
     */
    int getQueuedCommandCount();
    
    /**
     * Clear all queued commands.
     */
    void clearQueue();
}