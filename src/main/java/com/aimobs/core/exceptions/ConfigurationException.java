package com.aimobs.core.exceptions;

/**
 * Exception thrown when there are configuration or initialization errors.
 * This includes invalid service configurations, missing dependencies, or setup failures.
 */
public class ConfigurationException extends AIMobsException {
    
    public ConfigurationException(String message) {
        super(message);
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}