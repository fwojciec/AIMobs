package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.core.EntityActions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.util.math.Vec3d;

/**
 * Infrastructure adapter that implements EntityActions for Minecraft entities.
 * 
 * Following Ben Johnson's principles:
 * - Contains infrastructure-specific implementation details
 * - Adapts Minecraft APIs to our core interfaces
 * - Depends on core layer contracts only
 */
public class MinecraftEntityActions implements EntityActions {
    
    private final GoalSelector goalSelector;
    private final GoalSelector targetSelector;
    private final Entity entity;
    
    public MinecraftEntityActions(GoalSelector goalSelector, GoalSelector targetSelector, Entity entity) {
        this.goalSelector = goalSelector;
        this.targetSelector = targetSelector;
        this.entity = entity;
    }
    
    @Override
    public void clearGoals() {
        goalSelector.clear(goal -> true);
        targetSelector.clear(goal -> true);
    }
    
    @Override
    public void addSwimGoal() {
        // We'll need the entity reference for this - temporary implementation
        // TODO: Refactor to provide entity reference or use different approach
    }
    
    @Override
    public void addEscapeDangerGoal() {
        // TODO: Same issue - needs entity reference
    }
    
    @Override
    public void addControllableGoal() {
        // TODO: Same issue - needs entity reference
    }
    
    @Override
    public Vec3d getPosition() {
        return entity.getPos();
    }

    @Override
    public void addInteractionGoal(int priority, Object goal) {
        if (goal instanceof net.minecraft.entity.ai.goal.Goal) {
            goalSelector.add(priority, (net.minecraft.entity.ai.goal.Goal) goal);
        }
    }

    @Override
    public void removeInteractionGoal(Object goal) {
        if (goal instanceof net.minecraft.entity.ai.goal.Goal) {
            goalSelector.remove((net.minecraft.entity.ai.goal.Goal) goal);
        }
    }

    @Override
    public boolean canReachPosition(Vec3d targetPos) {
        // Basic implementation - could be improved with actual pathfinding check
        return entity.getPos().distanceTo(targetPos) <= 50.0;
    }

    @Override
    public net.minecraft.world.World getWorld() {
        return entity.getWorld();
    }

    @Override
    public net.minecraft.entity.passive.WolfEntity getWolfEntity() {
        if (entity instanceof net.minecraft.entity.passive.WolfEntity) {
            return (net.minecraft.entity.passive.WolfEntity) entity;
        }
        throw new IllegalStateException("Entity is not a WolfEntity: " + entity.getClass().getSimpleName());
    }
}