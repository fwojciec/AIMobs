package com.aimobs.network.core;

import java.util.Map;

/**
 * Core domain value object representing a network message.
 * Following Standard Package Layout - this is a domain primitive.
 * Immutable value object with validation.
 */
public class NetworkMessage {
    
    private String type;
    private String timestamp;
    private MessageData data;
    
    public NetworkMessage(String type, String timestamp, MessageData data) {
        this.type = type;
        this.timestamp = timestamp;
        this.data = data;
    }
    
    // Default constructor for testing
    public NetworkMessage() {
        this.type = null;
        this.timestamp = null;
        this.data = null;
    }
    
    public String getType() {
        return type;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public MessageData getData() {
        return data;
    }
    
    // Setters for testing
    public void setType(String type) {
        this.type = type;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public void setData(MessageData data) {
        this.data = data;
    }
    
    /**
     * Validate this message according to domain rules.
     * @return true if message is valid, false otherwise
     */
    public boolean isValid() {
        if (type == null || type.trim().isEmpty()) {
            return false;
        }
        
        if (timestamp == null || timestamp.trim().isEmpty()) {
            return false;
        }
        
        if (data == null) {
            return false;
        }
        
        return data.isValid();
    }
    
    /**
     * Nested value object for message data.
     */
    public static class MessageData {
        private String action;
        private Map<String, Object> parameters;
        private Map<String, Object> context;
        
        public MessageData(String action, Map<String, Object> parameters, Map<String, Object> context) {
            this.action = action;
            this.parameters = parameters;
            this.context = context;
        }
        
        // Default constructor for testing
        public MessageData() {
            this.action = null;
            this.parameters = null;
            this.context = null;
        }
        
        public String getAction() {
            return action;
        }
        
        public Map<String, Object> getParameters() {
            return parameters;
        }
        
        public Map<String, Object> getContext() {
            return context;
        }
        
        // Setters for testing
        public void setAction(String action) {
            this.action = action;
        }
        
        public void setParameters(Map<String, Object> parameters) {
            this.parameters = parameters;
        }
        
        public void setContext(Map<String, Object> context) {
            this.context = context;
        }
        
        public boolean isValid() {
            if (action == null || action.trim().isEmpty()) {
                return false;
            }
            
            // Validate action types according to PRD spec
            return isValidAction(action);
        }
        
        private boolean isValidAction(String action) {
            return "move".equals(action) || 
                   "follow".equals(action) ||
                   "stop".equals(action) ||
                   "comeHere".equals(action) ||
                   "attack".equals(action) || 
                   "collect".equals(action) ||
                   "defend".equals(action) ||
                   "speak".equals(action) ||
                   "communicate".equals(action) ||
                   "status".equals(action);
        }
    }
}