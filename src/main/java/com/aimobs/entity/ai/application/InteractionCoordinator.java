package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.InteractionService;
import com.aimobs.entity.ai.MovementService;
import com.aimobs.entity.ai.core.*;
import com.aimobs.entity.ai.infrastructure.AttackTargetGoal;
import com.aimobs.entity.ai.infrastructure.CollectItemsGoal;
import com.aimobs.entity.ai.infrastructure.DefendAreaGoal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Application layer implementation of InteractionService.
 * Contains pure business logic for interaction coordination.
 * 
 * Following Ben Johnson's standard package layout:
 * - Application layer implements service contracts
 * - Contains business logic but no infrastructure concerns
 * - Depends on root interfaces and core layer only
 */
public class InteractionCoordinator implements InteractionService {
    
    private final EntityActions entityActions;
    private final InventoryActions inventoryActions;
    private final MovementService movementService;
    
    private AIState currentState = AIState.IDLE;
    private Vec3d currentTargetPosition;
    private Object currentGoal;
    
    public InteractionCoordinator(EntityActions entityActions, InventoryActions inventoryActions, 
                                MovementService movementService) {
        this.entityActions = entityActions;
        this.inventoryActions = inventoryActions;
        this.movementService = movementService;
    }
    
    @Override
    public void attackTarget(TargetEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }
        
        stopAllInteractions();
        
        AttackTargetCommand command = new AttackTargetCommand(target);
        AttackTargetGoal goal = new AttackTargetGoal(
            entityActions.getWolfEntity(), 
            this, 
            command
        );
        
        entityActions.addInteractionGoal(5, goal);
        currentGoal = goal;
        currentState = AIState.ATTACKING;
        currentTargetPosition = target.getPosition();
    }
    
    @Override
    public void collectItems(String itemType, double radius, int maxItems) {
        if (itemType == null || itemType.trim().isEmpty() || radius <= 0) {
            return;
        }
        
        stopAllInteractions();
        
        CollectItemsCommand command = new CollectItemsCommand(itemType, radius, maxItems);
        CollectItemsGoal goal = new CollectItemsGoal(
            entityActions.getWolfEntity(),
            this,
            inventoryActions,
            command
        );
        
        entityActions.addInteractionGoal(3, goal);
        currentGoal = goal;
        currentState = AIState.COLLECTING;
        currentTargetPosition = entityActions.getPosition();
    }
    
    @Override
    public void defendArea(BlockPos centerPos, double radius) {
        if (centerPos == null || radius <= 0) {
            return;
        }
        
        stopAllInteractions();
        
        DefendAreaCommand command = new DefendAreaCommand(centerPos, radius);
        DefendAreaGoal goal = new DefendAreaGoal(
            entityActions.getWolfEntity(),
            this,
            command
        );
        
        entityActions.addInteractionGoal(4, goal);
        currentGoal = goal;
        currentState = AIState.DEFENDING;
        currentTargetPosition = new Vec3d(centerPos.getX(), centerPos.getY(), centerPos.getZ());
    }
    
    @Override
    public String processCommunication(String message) {
        if (message == null || message.trim().isEmpty()) {
            return null;
        }
        
        String lowerMessage = message.toLowerCase().trim();
        
        // Process different types of communication
        if (lowerMessage.contains("what do you see") || lowerMessage.contains("status")) {
            return generateStatusReport();
        } else if (lowerMessage.contains("how are you") || lowerMessage.contains("hello")) {
            return generateGreeting();
        } else if (lowerMessage.contains("inventory") || lowerMessage.contains("items")) {
            return generateInventoryReport();
        } else {
            return generateGenericResponse(message);
        }
    }
    
    @Override
    public void coordinatePositioning(Vec3d targetPosition) {
        if (targetPosition == null) {
            return;
        }
        
        // Check if we can reach the target position
        if (!entityActions.canReachPosition(targetPosition)) {
            return;
        }
        
        // Use movement service to coordinate positioning
        // This allows interaction goals to request movement assistance
        Vec3d currentPos = entityActions.getPosition();
        double distance = currentPos.distanceTo(targetPosition);
        
        // Only request movement if we're far from target
        if (distance > 3.0) {
            // Create a temporary movement target for positioning
            com.aimobs.entity.ai.core.MovementTarget movementTarget = 
                new com.aimobs.entity.ai.core.MovementTarget(targetPosition);
            movementService.moveTo(movementTarget);
        }
    }
    
    @Override
    public void stopAllInteractions() {
        if (currentGoal != null) {
            entityActions.removeInteractionGoal(currentGoal);
            currentGoal = null;
        }
        
        currentState = AIState.IDLE;
        currentTargetPosition = null;
    }
    
    @Override
    public void updateInteractionProgress() {
        if (currentGoal == null) {
            if (currentState != AIState.IDLE) {
                currentState = AIState.IDLE;
                currentTargetPosition = null;
            }
            return;
        }
        
        // Check if current goal is still valid
        boolean goalComplete = false;
        
        if (currentGoal instanceof AttackTargetGoal) {
            AttackTargetGoal attackGoal = (AttackTargetGoal) currentGoal;
            goalComplete = attackGoal.getCommand().isComplete();
        } else if (currentGoal instanceof CollectItemsGoal) {
            CollectItemsGoal collectGoal = (CollectItemsGoal) currentGoal;
            goalComplete = collectGoal.getCommand().isComplete();
        } else if (currentGoal instanceof DefendAreaGoal) {
            DefendAreaGoal defendGoal = (DefendAreaGoal) currentGoal;
            goalComplete = defendGoal.getCommand().isComplete();
        }
        
        if (goalComplete) {
            stopAllInteractions();
        }
    }
    
    @Override
    public AIState getCurrentState() {
        return currentState;
    }
    
    @Override
    public boolean isInteracting() {
        return currentState != AIState.IDLE && currentGoal != null;
    }
    
    @Override
    public Vec3d getCurrentTargetPosition() {
        return currentTargetPosition;
    }
    
    private String generateStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("Current state: ").append(currentState.name().toLowerCase());
        
        Vec3d position = entityActions.getPosition();
        report.append(". Position: (")
              .append(String.format("%.1f", position.x)).append(", ")
              .append(String.format("%.1f", position.y)).append(", ")
              .append(String.format("%.1f", position.z)).append(")");
        
        if (currentTargetPosition != null) {
            report.append(". Target: (")
                  .append(String.format("%.1f", currentTargetPosition.x)).append(", ")
                  .append(String.format("%.1f", currentTargetPosition.y)).append(", ")
                  .append(String.format("%.1f", currentTargetPosition.z)).append(")");
        }
        
        return report.toString();
    }
    
    private String generateGreeting() {
        switch (currentState) {
            case ATTACKING:
                return "I'm currently fighting threats!";
            case COLLECTING:
                return "I'm busy collecting items for you.";
            case DEFENDING:
                return "I'm on guard duty, keeping the area safe.";
            case MOVING:
                return "I'm on my way somewhere.";
            default:
                return "Hello! I'm ready for your commands.";
        }
    }
    
    private String generateInventoryReport() {
        int itemCount = inventoryActions.getItemCount();
        int maxCapacity = inventoryActions.getMaxCapacity();
        
        return String.format("I'm carrying %d/%d items.", itemCount, maxCapacity);
    }
    
    private String generateGenericResponse(String message) {
        return "I heard you say: \"" + message + "\". How can I help?";
    }
}