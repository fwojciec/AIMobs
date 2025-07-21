package com.aimobs.test;

import com.aimobs.entity.ai.PathfindingService;
import com.aimobs.entity.ai.core.MovementTarget;
import net.minecraft.util.math.Vec3d;

/**
 * Fake implementation of PathfindingService for testing.
 * Follows the fake object pattern used throughout the test suite.
 */
public class FakePathfindingService implements PathfindingService {
    
    private boolean isMoving = false;
    private boolean hasReachedTarget = false;
    private boolean hasPathfindingFailed = false;
    private boolean canReachTarget = true;
    private double movementSpeed = 1.0;
    private MovementTarget currentTarget = null;
    private Vec3d currentOrigin = null;

    @Override
    public void startPathfinding(Vec3d origin, MovementTarget target) {
        this.currentOrigin = origin;
        this.currentTarget = target;
        this.isMoving = true;
        this.hasReachedTarget = false;
        this.hasPathfindingFailed = false;
    }

    @Override
    public void stopPathfinding() {
        this.isMoving = false;
        this.currentTarget = null;
        this.currentOrigin = null;
    }

    @Override
    public boolean isMoving() {
        return isMoving;
    }

    @Override
    public boolean hasReachedTarget() {
        return hasReachedTarget;
    }

    @Override
    public boolean hasPathfindingFailed() {
        return hasPathfindingFailed;
    }

    @Override
    public boolean canReachTarget(Vec3d origin, MovementTarget target) {
        return canReachTarget;
    }

    @Override
    public double getMovementSpeed() {
        return movementSpeed;
    }

    @Override
    public void setMovementSpeed(double speed) {
        this.movementSpeed = speed;
    }

    // Test control methods
    public void setMoving(boolean moving) {
        this.isMoving = moving;
    }

    public void setHasReachedTarget(boolean reachedTarget) {
        this.hasReachedTarget = reachedTarget;
    }

    public void setHasPathfindingFailed(boolean failed) {
        this.hasPathfindingFailed = failed;
    }

    public void setCanReachTarget(boolean canReach) {
        this.canReachTarget = canReach;
    }

    public MovementTarget getCurrentTarget() {
        return currentTarget;
    }

    public Vec3d getCurrentOrigin() {
        return currentOrigin;
    }

    public void reset() {
        isMoving = false;
        hasReachedTarget = false;
        hasPathfindingFailed = false;
        canReachTarget = true;
        movementSpeed = 1.0;
        currentTarget = null;
        currentOrigin = null;
    }
}