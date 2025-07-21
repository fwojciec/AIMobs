package com.aimobs.entity.ai.core;

import java.util.Objects;

/**
 * Value object representing the AI state of an entity.
 * Contains the persistent state needed to restore AI capabilities.
 * 
 * Core domain object - no dependencies, pure data.
 */
public final class AiEntityState {
    private final boolean isAiControlled;
    private final AIState currentState;
    private final String entityType;

    public AiEntityState(boolean isAiControlled, AIState currentState, String entityType) {
        this.isAiControlled = isAiControlled;
        this.currentState = currentState;
        this.entityType = Objects.requireNonNull(entityType, "Entity type cannot be null");
    }

    public static AiEntityState createAiControlled(AIState currentState, String entityType) {
        return new AiEntityState(true, currentState, entityType);
    }

    public static AiEntityState createNonAi(String entityType) {
        return new AiEntityState(false, AIState.IDLE, entityType);
    }

    public boolean isAiControlled() {
        return isAiControlled;
    }

    public AIState getCurrentState() {
        return currentState;
    }

    public String getEntityType() {
        return entityType;
    }

    public AiEntityState withState(AIState newState) {
        return new AiEntityState(isAiControlled, newState, entityType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AiEntityState that = (AiEntityState) obj;
        return isAiControlled == that.isAiControlled &&
               Objects.equals(currentState, that.currentState) &&
               Objects.equals(entityType, that.entityType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isAiControlled, currentState, entityType);
    }

    @Override
    public String toString() {
        return "AiEntityState{" +
               "isAiControlled=" + isAiControlled +
               ", currentState=" + currentState +
               ", entityType='" + entityType + '\'' +
               '}';
    }
}