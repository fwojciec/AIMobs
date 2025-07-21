package com.aimobs.entity.ai.core;

/**
 * Enumeration of different interaction types that the AI can perform.
 * 
 * Part of the core layer - domain primitive with no dependencies.
 * Following Ben Johnson's standard package layout principles.
 */
public enum InteractionType {
    ATTACK(5),
    COLLECT(3),
    DEFEND(4),
    COMMUNICATE(1);
    
    private final int defaultPriority;
    
    InteractionType(int defaultPriority) {
        this.defaultPriority = defaultPriority;
    }
    
    /**
     * @return The default priority level for this interaction type
     */
    public int getDefaultPriority() {
        return defaultPriority;
    }
}