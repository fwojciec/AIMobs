package com.aimobs.network.application;

import com.aimobs.entity.ai.CommandProcessorService;
import com.aimobs.entity.ai.CommandRoutingService;
import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.AiControlledWolfEntity;
import com.aimobs.network.MessageService;
import com.aimobs.network.core.NetworkMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Application service implementing message parsing and command queuing.
 * Following Standard Package Layout - this is business logic implementation.
 */
public class MessageParser implements MessageService {
    
    private final Gson gson;
    private final Queue<AICommand> commandQueue;
    private final CommandProcessorService commandProcessor;
    private CommandRoutingService commandRouter; // Not final - can be set dynamically
    
    public MessageParser(CommandProcessorService commandProcessor, CommandRoutingService commandRouter) {
        this.gson = new Gson();
        this.commandQueue = new ConcurrentLinkedQueue<>();
        this.commandProcessor = commandProcessor;
        this.commandRouter = commandRouter;
    }
    
    
    // Method to set the command router for entity lookup and routing
    public void setCommandRouter(CommandRoutingService commandRouter) {
        this.commandRouter = commandRouter;
        System.out.println("[AIMobs] CommandRouter configured on MessageParser");
    }
    
    @Override
    public NetworkMessage parseMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Parse JSON into temporary structure
            JsonMessage jsonMessage = gson.fromJson(rawMessage, JsonMessage.class);
            
            if (jsonMessage == null) {
                return null;
            }
            
            // Convert to domain object
            NetworkMessage.MessageData data = new NetworkMessage.MessageData(
                jsonMessage.data != null ? jsonMessage.data.action : null,
                jsonMessage.data != null ? jsonMessage.data.parameters : Map.of(),
                jsonMessage.data != null ? jsonMessage.data.context : Map.of()
            );
            
            return new NetworkMessage(jsonMessage.type, jsonMessage.timestamp, data);
            
        } catch (JsonSyntaxException e) {
            // Invalid JSON - return null
            return null;
        }
    }
    
    @Override
    public boolean validateMessage(NetworkMessage message) {
        return message != null && message.isValid();
    }
    
    @Override
    public AICommand convertToCommand(NetworkMessage message) {
        if (!validateMessage(message)) {
            return null;
        }
        
        String action = message.getData().getAction();
        
        // Use command router to create commands through proper command receivers
        if (commandRouter != null) {
            // Find any available command receiver to create the command
            java.util.Optional<com.aimobs.entity.ai.CommandReceiver> receiver = 
                ((com.aimobs.entity.ai.application.CommandRouter) commandRouter).getEntityLookup().findAnyAvailable();
            
            if (receiver.isPresent() && receiver.get() instanceof com.aimobs.entity.AiControlledWolfEntity wolf) {
                // Use the wolf to create appropriate commands
                if (isMovementCommand(action)) {
                    AICommand movementCommand = wolf.createMovementCommand(message);
                    if (movementCommand != null) {
                        return movementCommand;
                    }
                }
                
                if (isInteractionCommand(action)) {
                    AICommand interactionCommand = wolf.createInteractionCommand(message);
                    if (interactionCommand != null) {
                        return interactionCommand;
                    }
                }
            }
        }
        
        // Fall back to generic network command
        return new NetworkAICommand(message);
    }
    
    private boolean isMovementCommand(String action) {
        return "move".equals(action) || 
               "follow".equals(action) || 
               "stop".equals(action) || 
               "comeHere".equals(action);
    }
    
    private boolean isInteractionCommand(String action) {
        return "attack".equals(action) || 
               "collect".equals(action) || 
               "defend".equals(action) || 
               "speak".equals(action) ||
               "communicate".equals(action);
    }
    
    @Override
    public void queueCommand(AICommand command) {
        if (command != null) {
            // Route through the command router
            if (commandRouter != null && commandRouter.routeToAnyAvailable(command)) {
                return; // Successfully routed
            }
            
            // Fallback to local queue if no entity is available
            commandQueue.offer(command);
        }
    }
    
    @Override
    public int getQueuedCommandCount() {
        return commandQueue.size();
    }
    
    @Override
    public void clearQueue() {
        commandQueue.clear();
    }
    
    /**
     * Temporary JSON structure for parsing.
     */
    private static class JsonMessage {
        String type;
        String timestamp;
        JsonData data;
        
        static class JsonData {
            String action;
            Map<String, Object> parameters;
            Map<String, Object> context;
        }
    }
    
    /**
     * AI Command implementation that wraps network messages.
     */
    private static class NetworkAICommand implements AICommand {
        private final NetworkMessage message;
        private boolean executed = false;
        private boolean cancelled = false;
        
        public NetworkAICommand(NetworkMessage message) {
            this.message = message;
        }
        
        @Override
        public void execute() {
            if (!cancelled) {
                // Actual execution would be delegated to appropriate command handlers
                executed = true;
            }
        }
        
        @Override
        public boolean isComplete() {
            return executed || cancelled;
        }
        
        @Override
        public void cancel() {
            cancelled = true;
        }
        
        public NetworkMessage getMessage() {
            return message;
        }
    }
}