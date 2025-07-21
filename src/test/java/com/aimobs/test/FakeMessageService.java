package com.aimobs.test;

import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.network.MessageService;
import com.aimobs.network.core.NetworkMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fake implementation - no mocking framework needed.
 * Following Feathers: "Prefer fake objects to mock objects"
 * Completely deterministic and controllable.
 */
public class FakeMessageService implements MessageService {
    
    private final Gson gson = new Gson();
    private final List<AICommand> commandQueue = new ArrayList<>();
    private boolean shouldValidateAsTrue = true;
    private boolean shouldParseSuccessfully = true;
    
    @Override
    public NetworkMessage parseMessage(String rawMessage) {
        if (!shouldParseSuccessfully || rawMessage == null || rawMessage.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Simple parsing for test purposes - look for action with or without spaces
            if (rawMessage.contains("\"action\"") && 
                (rawMessage.contains("\"move\"") || rawMessage.contains(": \"move\""))) {
                NetworkMessage.MessageData data = new NetworkMessage.MessageData(
                    "move", Map.of("x", 10), Map.of()
                );
                return new NetworkMessage("command", "2025-01-19T10:00:00Z", data);
            }
            return null;
        } catch (JsonSyntaxException e) {
            // Invalid JSON format
            return null;
        }
    }
    
    @Override
    public boolean validateMessage(NetworkMessage message) {
        return shouldValidateAsTrue && message != null && message.isValid();
    }
    
    @Override
    public AICommand convertToCommand(NetworkMessage message) {
        if (!validateMessage(message)) {
            return null;
        }
        return new FakeAICommand(message);
    }
    
    @Override
    public void queueCommand(AICommand command) {
        if (command != null) {
            commandQueue.add(command);
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
    
    // Test control methods
    public void setShouldValidate(boolean shouldValidate) {
        this.shouldValidateAsTrue = shouldValidate;
    }
    
    public void setShouldParseSuccessfully(boolean shouldParse) {
        this.shouldParseSuccessfully = shouldParse;
    }
    
    public List<AICommand> getQueuedCommands() {
        return new ArrayList<>(commandQueue);
    }
    
    public void reset() {
        commandQueue.clear();
        shouldValidateAsTrue = true;
        shouldParseSuccessfully = true;
    }
    
    /**
     * Simple fake AI command for testing.
     */
    private static class FakeAICommand implements AICommand {
        private final NetworkMessage message;
        private boolean executed = false;
        private boolean cancelled = false;
        
        public FakeAICommand(NetworkMessage message) {
            this.message = message;
        }
        
        @Override
        public void execute() {
            executed = true;
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