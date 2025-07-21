package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.MovementTarget;
import net.minecraft.util.math.Vec3d;

/**
 * Service contract for low-level pathfinding and navigation.
 * Abstracts the underlying Minecraft navigation system to enable
 * testing and alternative implementations.
 */
public interface PathfindingService {
    
    /**
     * Starts pathfinding from the origin to the target.
     * 
     * @param origin The starting position
     * @param target The destination target
     */
    void startPathfinding(Vec3d origin, MovementTarget target);
    
    /**
     * Stops all current pathfinding and movement.
     */
    void stopPathfinding();
    
    /**
     * @return true if the entity is currently moving
     */
    boolean isMoving();
    
    /**
     * @return true if the entity has reached its current target
     */
    boolean hasReachedTarget();
    
    /**
     * @return true if pathfinding has failed (stuck, unreachable, etc.)
     */
    boolean hasPathfindingFailed();
    
    /**
     * Checks if a target can be reached from the origin.
     * This is typically faster than actually starting pathfinding.
     * 
     * @param origin The starting position
     * @param target The destination target
     * @return true if the target appears reachable
     */
    boolean canReachTarget(Vec3d origin, MovementTarget target);
    
    /**
     * Gets the current movement speed multiplier.
     * 
     * @return The speed multiplier (1.0 = normal speed)
     */
    double getMovementSpeed();
    
    /**
     * Sets the movement speed multiplier for subsequent movements.
     * 
     * @param speed The speed multiplier (1.0 = normal speed)
     */
    void setMovementSpeed(double speed);
}