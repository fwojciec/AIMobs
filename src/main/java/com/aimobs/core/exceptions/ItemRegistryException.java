package com.aimobs.core.exceptions;

/**
 * Exception thrown when item registry operations fail.
 * This includes invalid item identifiers, missing registry entries, or type conversion failures.
 */
public class ItemRegistryException extends AIMobsException {
    
    public ItemRegistryException(String message) {
        super(message);
    }
    
    public ItemRegistryException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ItemRegistryException(Throwable cause) {
        super(cause);
    }
}