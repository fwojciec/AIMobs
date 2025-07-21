package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.PathfindingService;
import com.aimobs.entity.ai.core.MovementTarget;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Infrastructure adapter implementing pathfinding through Minecraft's navigation system.
 * Wraps EntityNavigation to provide a clean interface for the application layer.
 */
public class MinecraftPathfindingService implements PathfindingService {
    
    private final WolfEntity entity;
    private final EntityNavigation navigation;
    private MovementTarget currentTarget;
    private double movementSpeed = 1.0;
    private boolean pathfindingStarted = false;

    public MinecraftPathfindingService(WolfEntity entity) {
        this.entity = entity;
        this.navigation = entity.getNavigation();
    }

    @Override
    public void startPathfinding(Vec3d origin, MovementTarget target) {
        this.currentTarget = target;
        this.pathfindingStarted = true;
        
        Vec3d targetPos = target.getPosition();
        navigation.startMovingTo(targetPos.x, targetPos.y, targetPos.z, movementSpeed);
    }

    @Override
    public void stopPathfinding() {
        navigation.stop();
        currentTarget = null;
        pathfindingStarted = false;
    }

    @Override
    public boolean isMoving() {
        return !navigation.isIdle();
    }

    @Override
    public boolean hasReachedTarget() {
        if (currentTarget == null) {
            return false;
        }
        
        // Check if navigation is idle (reached destination or stopped)
        if (!navigation.isIdle()) {
            return false;
        }
        
        // Check if we're actually close to the target
        Vec3d currentPos = entity.getPos();
        double distanceToTarget = currentTarget.distanceFrom(currentPos);
        
        return distanceToTarget <= 2.0; // Within 2 blocks of target
    }

    @Override
    public boolean hasPathfindingFailed() {
        if (currentTarget == null || !pathfindingStarted) {
            return false;
        }
        
        // Consider pathfinding failed if:
        // 1. Navigation is idle (not moving)
        // 2. We haven't reached the target
        // 3. We had started pathfinding
        return navigation.isIdle() && !hasReachedTarget();
    }

    @Override
    public boolean canReachTarget(Vec3d origin, MovementTarget target) {
        Vec3d targetPos = target.getPosition();
        
        // Use Minecraft's pathfinding to check if target is reachable
        // This is a lightweight check that doesn't start actual movement
        return navigation.findPathTo(targetPos.x, targetPos.y, targetPos.z, 0) != null;
    }

    @Override
    public double getMovementSpeed() {
        return movementSpeed;
    }

    @Override
    public void setMovementSpeed(double speed) {
        this.movementSpeed = Math.max(0.1, Math.min(3.0, speed)); // Clamp between 0.1 and 3.0
    }
}