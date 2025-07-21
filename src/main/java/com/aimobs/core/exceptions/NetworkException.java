package com.aimobs.core.exceptions;

/**
 * Exception thrown when network operations fail.
 * This includes WebSocket connections, message parsing, and communication errors.
 */
public class NetworkException extends AIMobsException {
    
    public NetworkException(String message) {
        super(message);
    }
    
    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public NetworkException(Throwable cause) {
        super(cause);
    }
}