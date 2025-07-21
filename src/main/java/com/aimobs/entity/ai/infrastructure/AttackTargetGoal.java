package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.InteractionService;
import com.aimobs.entity.ai.core.AIState;
import com.aimobs.entity.ai.core.AttackTargetCommand;
import com.aimobs.entity.ai.core.LivingEntityTarget;
import com.aimobs.entity.ai.core.TargetEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

/**
 * Minecraft AI Goal for attacking specific targets.
 * Infrastructure layer - platform-specific implementation.
 * 
 * Following Ben Johnson's standard package layout:
 * - Infrastructure layer adapts external systems to internal interfaces
 * - Can depend on all other layers
 */
public class AttackTargetGoal extends Goal {
    
    private final WolfEntity wolf;
    private final InteractionService interactionService;
    private final AttackTargetCommand command;
    private final double attackRange = 2.0;
    private final double approachRange = 8.0;
    
    public AttackTargetGoal(WolfEntity wolf, InteractionService interactionService, AttackTargetCommand command) {
        this.wolf = wolf;
        this.interactionService = interactionService;
        this.command = command;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.TARGET));
    }
    
    @Override
    public boolean canStart() {
        if (command.isComplete() || command.isCancelled()) {
            return false;
        }
        
        TargetEntity target = command.getTarget();
        if (target == null || !target.isAlive()) {
            command.cancel();
            return false;
        }
        
        // Extract Minecraft entity for infrastructure operations
        LivingEntity minecraftTarget = extractMinecraftEntity(target);
        if (minecraftTarget == null) {
            command.cancel();
            return false;
        }
        
        // Check if target is within reasonable range
        double distance = wolf.distanceTo(minecraftTarget);
        return distance <= approachRange;
    }
    
    @Override
    public boolean shouldContinue() {
        if (command.isComplete() || command.isCancelled()) {
            return false;
        }
        
        TargetEntity target = command.getTarget();
        if (target == null || !target.isAlive()) {
            command.cancel();
            return false;
        }
        
        // Extract Minecraft entity for infrastructure operations
        LivingEntity minecraftTarget = extractMinecraftEntity(target);
        if (minecraftTarget == null) {
            command.cancel();
            return false;
        }
        
        // Continue if target is still within approach range
        double distance = wolf.distanceTo(minecraftTarget);
        return distance <= approachRange;
    }
    
    @Override
    public void start() {
        TargetEntity target = command.getTarget();
        if (target != null) {
            // Extract Minecraft entity for infrastructure operations
            LivingEntity minecraftTarget = extractMinecraftEntity(target);
            if (minecraftTarget != null) {
                wolf.setTarget(minecraftTarget);
                
                // Request positioning if not in attack range
                double distance = wolf.distanceTo(minecraftTarget);
                if (distance > attackRange) {
                    Vec3d targetPos = target.getPosition();
                    interactionService.coordinatePositioning(targetPos);
                }
            }
        }
    }
    
    @Override
    public void tick() {
        TargetEntity target = command.getTarget();
        if (target == null || !target.isAlive()) {
            command.cancel();
            return;
        }
        
        // Extract Minecraft entity for infrastructure operations
        LivingEntity minecraftTarget = extractMinecraftEntity(target);
        if (minecraftTarget == null) {
            command.cancel();
            return;
        }
        
        double distance = wolf.distanceTo(minecraftTarget);
        
        if (distance <= attackRange) {
            // In attack range - perform attack
            wolf.tryAttack(minecraftTarget);
        } else if (distance <= approachRange) {
            // Move closer to target
            wolf.getNavigation().startMovingTo(minecraftTarget, 1.2);
        } else {
            // Target too far away - cancel attack
            command.cancel();
        }
        
        // Update interaction progress
        interactionService.updateInteractionProgress();
    }
    
    @Override
    public void stop() {
        wolf.setTarget(null);
        wolf.getNavigation().stop();
    }
    
    @Override
    public boolean canStop() {
        // Allow higher priority goals to interrupt
        return true;
    }
    
    /**
     * @return The attack command this goal is executing
     */
    public AttackTargetCommand getCommand() {
        return command;
    }
    
    /**
     * Extracts the Minecraft LivingEntity from our TargetEntity abstraction.
     * This is needed for infrastructure layer operations that require the actual Minecraft entity.
     */
    private LivingEntity extractMinecraftEntity(TargetEntity target) {
        if (target instanceof LivingEntityTarget) {
            return ((LivingEntityTarget) target).getLivingEntity();
        }
        return null; // Can't work with non-Minecraft entities in this goal
    }
}