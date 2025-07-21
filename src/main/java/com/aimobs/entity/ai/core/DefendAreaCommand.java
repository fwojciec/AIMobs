package com.aimobs.entity.ai.core;

import net.minecraft.util.math.BlockPos;

/**
 * Command for defending a specific area from hostile entities.
 * 
 * Part of the core layer - pure domain object with no dependencies.
 * Following Ben Johnson's standard package layout principles.
 */
public class DefendAreaCommand implements InteractionCommand {
    
    private final BlockPos centerPos;
    private final double radius;
    private final int priority;
    private boolean isComplete = false;
    private boolean isCancelled = false;
    
    public DefendAreaCommand(BlockPos centerPos, double radius) {
        this(centerPos, radius, InteractionType.DEFEND.getDefaultPriority());
    }
    
    public DefendAreaCommand(BlockPos centerPos, double radius, int priority) {
        this.centerPos = centerPos;
        this.radius = radius;
        this.priority = priority;
    }
    
    @Override
    public void execute() {
        if (isCancelled) {
            isComplete = true;
            return;
        }
        // Execution logic will be handled by the goal system
        // Defense is ongoing until cancelled
    }
    
    @Override
    public boolean isComplete() {
        return isComplete || isCancelled;
    }
    
    @Override
    public void cancel() {
        isCancelled = true;
        isComplete = true;
    }
    
    @Override
    public InteractionType getInteractionType() {
        return InteractionType.DEFEND;
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
     * @return The center position of the area to defend
     */
    public BlockPos getCenterPos() {
        return centerPos;
    }
    
    /**
     * @return The radius of the area to defend
     */
    public double getRadius() {
        return radius;
    }
    
    /**
     * @return True if the command was cancelled
     */
    public boolean isCancelled() {
        return isCancelled;
    }
}