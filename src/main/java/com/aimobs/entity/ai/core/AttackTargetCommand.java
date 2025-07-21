package com.aimobs.entity.ai.core;

/**
 * Command for attacking a specific target entity.
 * 
 * Part of the core layer - pure domain object with no dependencies.
 * Following Ben Johnson's standard package layout principles.
 */
public class AttackTargetCommand implements InteractionCommand {
    
    private final TargetEntity target;
    private final int priority;
    private boolean isComplete = false;
    private boolean isCancelled = false;
    
    public AttackTargetCommand(TargetEntity target) {
        this(target, InteractionType.ATTACK.getDefaultPriority());
    }
    
    public AttackTargetCommand(TargetEntity target, int priority) {
        this.target = target;
        this.priority = priority;
    }
    
    @Override
    public void execute() {
        if (isCancelled || target == null || !target.isAlive()) {
            isComplete = true;
            return;
        }
        // Execution logic will be handled by the goal system
    }
    
    @Override
    public boolean isComplete() {
        return isComplete || isCancelled || (target != null && !target.isAlive());
    }
    
    @Override
    public void cancel() {
        isCancelled = true;
        isComplete = true;
    }
    
    @Override
    public InteractionType getInteractionType() {
        return InteractionType.ATTACK;
    }
    
    @Override
    public boolean requiresPositioning() {
        return true;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    /**
     * @return The target entity to attack
     */
    public TargetEntity getTarget() {
        return target;
    }
    
    /**
     * @return True if the command was cancelled
     */
    public boolean isCancelled() {
        return isCancelled;
    }
}