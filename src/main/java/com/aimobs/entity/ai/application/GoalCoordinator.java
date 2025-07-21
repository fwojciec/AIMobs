package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.core.EntityActions;

import com.aimobs.entity.ai.GoalService;

/**
 * Application layer implementation of GoalService.
 * 
 * Following standard package layout:
 * - Implements interface from root package
 * - Pure business logic with no infrastructure dependencies
 * - Depends only on core layer interfaces
 */
public class GoalCoordinator implements GoalService {
    
    private final EntityActions entityActions;
    
    public GoalCoordinator(EntityActions entityActions) {
        this.entityActions = entityActions;
    }
    
    /**
     * Initialize goals for AI-controlled entity.
     * Removes default behaviors and adds essential survival + controllable goals.
     */
    public void initializeAIGoals() {
        entityActions.clearGoals();
        entityActions.addSwimGoal();
        entityActions.addEscapeDangerGoal();
        entityActions.addControllableGoal();
    }
}