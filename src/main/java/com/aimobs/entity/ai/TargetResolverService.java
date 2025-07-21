package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.MovementTarget;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Service contract for resolving string-based targets to movement destinations.
 * Handles various target formats including coordinates, relative positions,
 * and object references.
 */
public interface TargetResolverService {
    
    /**
     * Resolves a string target to a concrete movement destination.
     * 
     * Supported formats:
     * - Coordinates: "10 64 10"
     * - Relative: "north 5", "south 10", "east 3", "west 7"
     * - Objects: "tree", "house", "player" (future enhancement)
     * 
     * @param target The target string to resolve
     * @param world The world context for resolution
     * @param origin The origin position for relative calculations
     * @return Optional containing the resolved target, or empty if invalid
     */
    Optional<MovementTarget> resolveTarget(String target, World world, Vec3d origin);
    
    /**
     * Validates if a target string is potentially resolvable.
     * This is a quick check that doesn't require world access.
     * 
     * @param target The target string to validate
     * @return true if the target format is recognized
     */
    boolean isValidTargetFormat(String target);
    
    /**
     * Checks if a resolved target is reachable from the origin.
     * Considers distance limitations and basic accessibility.
     * 
     * @param target The movement target to check
     * @param origin The starting position
     * @param world The world context
     * @return true if the target appears reachable
     */
    boolean isTargetReachable(MovementTarget target, Vec3d origin, World world);
}