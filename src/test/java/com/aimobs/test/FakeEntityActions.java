package com.aimobs.test;

import com.aimobs.entity.ai.core.EntityActions;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake implementation of EntityActions for testing.
 * Follows the fake object pattern used throughout the test suite.
 */
public class FakeEntityActions implements EntityActions {
    
    private Vec3d position = new Vec3d(0, 64, 0);
    private boolean goalsCleared = false;
    private boolean swimGoalAdded = false;
    private boolean escapeDangerGoalAdded = false;
    private boolean controllableGoalAdded = false;
    private final List<Object> interactionGoals = new ArrayList<>();
    private final List<Integer> goalPriorities = new ArrayList<>();

    @Override
    public void clearGoals() {
        goalsCleared = true;
    }

    @Override
    public void addSwimGoal() {
        swimGoalAdded = true;
    }

    @Override
    public void addEscapeDangerGoal() {
        escapeDangerGoalAdded = true;
    }

    @Override
    public void addControllableGoal() {
        controllableGoalAdded = true;
    }

    @Override
    public Vec3d getPosition() {
        return position;
    }

    @Override
    public void addInteractionGoal(int priority, Object goal) {
        interactionGoals.add(goal);
        goalPriorities.add(priority);
    }

    @Override
    public void removeInteractionGoal(Object goal) {
        int index = interactionGoals.indexOf(goal);
        if (index >= 0) {
            interactionGoals.remove(index);
            goalPriorities.remove(index);
        }
    }

    @Override
    public boolean canReachPosition(Vec3d targetPos) {
        // For testing, assume all positions within 20 blocks are reachable
        return position.distanceTo(targetPos) <= 20.0;
    }

    @Override
    public World getWorld() {
        // Return null for testing - tests should mock world interactions separately
        return null;
    }

    @Override
    public net.minecraft.entity.passive.WolfEntity getWolfEntity() {
        // Return null for testing - infrastructure tests should use real implementations
        return null;
    }

    // Test control methods
    public void setPosition(Vec3d position) {
        this.position = position;
    }

    public boolean isGoalsCleared() {
        return goalsCleared;
    }

    public boolean isSwimGoalAdded() {
        return swimGoalAdded;
    }

    public boolean isEscapeDangerGoalAdded() {
        return escapeDangerGoalAdded;
    }

    public boolean isControllableGoalAdded() {
        return controllableGoalAdded;
    }

    // Test helper methods
    public List<Object> getInteractionGoals() {
        return new ArrayList<>(interactionGoals);
    }

    public List<Integer> getGoalPriorities() {
        return new ArrayList<>(goalPriorities);
    }

    public int getInteractionGoalCount() {
        return interactionGoals.size();
    }

    public void reset() {
        position = new Vec3d(0, 64, 0);
        goalsCleared = false;
        swimGoalAdded = false;
        escapeDangerGoalAdded = false;
        controllableGoalAdded = false;
        interactionGoals.clear();
        goalPriorities.clear();
    }
}