package com.aimobs.core.exceptions;

/**
 * Base exception for all AIMobs-specific errors.
 * Provides a foundation for the exception hierarchy.
 */
public abstract class AIMobsException extends Exception {
    
    public AIMobsException(String message) {
        super(message);
    }
    
    public AIMobsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AIMobsException(Throwable cause) {
        super(cause);
    }
}