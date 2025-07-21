package com.aimobs.entity.ai.core;

/**
 * Enumeration of possible movement states for AI entities.
 * Represents the current movement activity and can be used for
 * state management and UI feedback.
 */
public enum MovementState {
    /**
     * Entity is not moving and has no movement goals.
     */
    IDLE,

    /**
     * Entity is moving towards a specific location.
     */
    MOVING_TO_LOCATION,

    /**
     * Entity is actively following a player.
     */
    FOLLOWING_PLAYER,

    /**
     * Entity attempted to find a path but failed.
     * May retry or require intervention.
     */
    PATHFINDING_FAILED
}