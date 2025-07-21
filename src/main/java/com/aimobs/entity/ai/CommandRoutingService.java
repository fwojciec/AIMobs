package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AICommand;

import java.util.Optional;

/**
 * Service contract for routing commands to appropriate command receivers.
 * Handles command delivery logic and routing decisions.
 * 
 * Root interface - defines what the system does (contract only).
 * Dependencies: Core layer only.
 */
public interface CommandRoutingService {
    
    /**
     * Routes a command to an appropriate receiver.
     * If targetId is provided, routes to specific entity.
     * Otherwise, routes to any available entity.
     * 
     * @param command The command to route
     * @param targetId Optional target entity ID for specific routing
     * @return true if command was successfully routed
     */
    boolean routeCommand(AICommand command, Optional<EntityId> targetId);
    
    /**
     * Routes a command to any available receiver.
     * Uses load balancing to distribute commands fairly.
     * 
     * @param command The command to route
     * @return true if command was successfully routed
     */
    boolean routeToAnyAvailable(AICommand command);
    
    /**
     * Gets the number of available command receivers.
     * Used for routing decisions and monitoring.
     * 
     * @return Number of available receivers
     */
    int getAvailableReceiverCount();
    
    /**
     * Checks if any receivers are available for command processing.
     * 
     * @return true if at least one receiver is available
     */
    boolean hasAvailableReceivers();
}