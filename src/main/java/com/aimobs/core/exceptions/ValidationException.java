package com.aimobs.core.exceptions;

/**
 * Exception thrown when input validation fails.
 * This includes invalid command parameters, malformed data, or constraint violations.
 */
public class ValidationException extends AIMobsException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ValidationException(Throwable cause) {
        super(cause);
    }
}