package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.EntityActions;

/**
 * Root package interface - defines goal coordination contract.
 * No dependencies on subpackages.
 * 
 * Following standard package layout:
 * - Root package contains interfaces
 * - Implementations provided by subpackages
 * - Composition happens at application boundary
 */
public interface GoalService {
    void initializeAIGoals();
}