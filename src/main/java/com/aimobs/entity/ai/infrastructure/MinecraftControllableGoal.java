package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.core.AIState;
import com.aimobs.entity.ai.core.CommandExecutor;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.WolfEntity;

/**
 * Infrastructure implementation of controllable goal for Minecraft entities.
 * 
 * Following Ben Johnson's principles:
 * - Infrastructure layer component
 * - Adapts Minecraft Goal system to our CommandExecutor interface
 * - No circular dependencies - uses interface, not concrete entity
 */
public class MinecraftControllableGoal extends Goal {
    
    private final CommandExecutor commandExecutor;
    
    public MinecraftControllableGoal(WolfEntity entity) {
        // We know the entity implements CommandExecutor, but use the interface
        this.commandExecutor = (CommandExecutor) entity;
    }
    
    @Override
    public boolean canStart() {
        return commandExecutor.getCurrentState() == AIState.BUSY;
    }
    
    @Override
    public void tick() {
        // Goal processing happens in the entity's tick method
        // This goal just ensures the entity can process commands when BUSY
    }
}