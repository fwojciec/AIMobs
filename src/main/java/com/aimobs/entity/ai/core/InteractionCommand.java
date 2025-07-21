package com.aimobs.entity.ai.core;

/**
 * Core interface for interaction commands.
 * Extends the base AICommand interface to provide interaction-specific behavior.
 * 
 * Part of the core layer - no dependencies on other layers.
 * Following Ben Johnson's standard package layout:
 * - Core layer contains only interfaces and value objects
 * - No dependencies on infrastructure or application layers
 */
public interface InteractionCommand extends AICommand {
    
    /**
     * @return The type of interaction this command performs
     */
    InteractionType getInteractionType();
    
    /**
     * @return True if this command requires positioning before execution
     */
    boolean requiresPositioning();
    
    /**
     * @return The priority level of this interaction (higher = more important)
     */
    int getPriority();
}