package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.CommandRoutingService;
import com.aimobs.entity.ai.EntityLookupService;
import com.aimobs.entity.ai.CommandReceiver;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AICommand;

import java.util.List;
import java.util.Optional;

/**
 * Application service implementing command routing logic.
 * Pure business logic for distributing commands to entities.
 * 
 * Application layer - implements service contracts.
 * Dependencies: Root interfaces + Core only.
 */
public class CommandRouter implements CommandRoutingService {
    
    private final EntityLookupService entityLookup;
    
    public CommandRouter(EntityLookupService entityLookup) {
        this.entityLookup = entityLookup;
    }
    
    @Override
    public boolean routeCommand(AICommand command, Optional<EntityId> targetId) {
        if (command == null) {
            return false;
        }
        
        if (targetId.isPresent()) {
            // Route to specific entity
            return routeToSpecificEntity(command, targetId.get());
        } else {
            // Route to any available entity
            return routeToAnyAvailable(command);
        }
    }
    
    @Override
    public boolean routeToAnyAvailable(AICommand command) {
        if (command == null) {
            return false;
        }
        
        Optional<CommandReceiver> receiver = entityLookup.findAnyAvailable();
        if (receiver.isPresent()) {
            receiver.get().receiveCommand(command);
            return true;
        }
        
        return false;
    }
    
    @Override
    public int getAvailableReceiverCount() {
        return entityLookup.getAvailableEntityCount();
    }
    
    @Override
    public boolean hasAvailableReceivers() {
        return getAvailableReceiverCount() > 0;
    }
    
    // Package-private getter for MessageParser to access entity lookup
    public EntityLookupService getEntityLookup() {
        return entityLookup;
    }
    
    /**
     * Routes a command to a specific entity by ID.
     * Pure business logic - no platform dependencies.
     */
    private boolean routeToSpecificEntity(AICommand command, EntityId targetId) {
        Optional<CommandReceiver> receiver = entityLookup.findEntityById(targetId);
        if (receiver.isPresent() && receiver.get().isAvailable()) {
            receiver.get().receiveCommand(command);
            return true;
        }
        
        return false;
    }
}