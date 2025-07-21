package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.TargetResolverService;
import com.aimobs.entity.ai.core.MovementTarget;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Application service implementing target resolution logic.
 * Converts string-based targets to concrete movement destinations.
 */
public class TargetResolver implements TargetResolverService {
    
    private static final Pattern COORDINATE_PATTERN = Pattern.compile("^(-?\\d+)\\s+(-?\\d+)\\s+(-?\\d+)$");
    private static final Pattern RELATIVE_PATTERN = Pattern.compile("^(north|south|east|west)\\s+(\\d+)$");
    private static final double MAX_MOVEMENT_DISTANCE = 100.0;
    private static final int MIN_Y_COORDINATE = 0;
    private static final int MAX_Y_COORDINATE = 256;

    @Override
    public Optional<MovementTarget> resolveTarget(String target, World world, Vec3d origin) {
        if (!isValidTargetFormat(target)) {
            return Optional.empty();
        }
        
        target = target.trim().toLowerCase();
        
        // Try coordinate resolution first
        Optional<MovementTarget> coordinateTarget = resolveCoordinateTarget(target);
        if (coordinateTarget.isPresent()) {
            MovementTarget resolved = coordinateTarget.get();
            if (isTargetReachable(resolved, origin, world)) {
                return coordinateTarget;
            }
            return Optional.empty();
        }
        
        // Try relative target resolution
        Optional<MovementTarget> relativeTarget = resolveRelativeTarget(target, origin);
        if (relativeTarget.isPresent()) {
            MovementTarget resolved = relativeTarget.get();
            if (isTargetReachable(resolved, origin, world)) {
                return relativeTarget;
            }
            return Optional.empty();
        }
        
        // Object resolution (future enhancement)
        return resolveObjectTarget(target, world, origin);
    }

    @Override
    public boolean isValidTargetFormat(String target) {
        if (target == null || target.trim().isEmpty()) {
            return false;
        }
        
        target = target.trim().toLowerCase();
        
        // Check coordinate format
        if (COORDINATE_PATTERN.matcher(target).matches()) {
            return true;
        }
        
        // Check relative format
        if (RELATIVE_PATTERN.matcher(target).matches()) {
            return true;
        }
        
        // Check object formats (basic validation)
        return target.matches("^[a-zA-Z]+$");
    }

    @Override
    public boolean isTargetReachable(MovementTarget target, Vec3d origin, World world) {
        // Distance check
        double distance = target.distanceFrom(origin);
        if (distance > MAX_MOVEMENT_DISTANCE) {
            return false;
        }
        
        // Y coordinate validation
        BlockPos blockPos = target.getBlockPos();
        if (blockPos.getY() < MIN_Y_COORDINATE || blockPos.getY() > MAX_Y_COORDINATE) {
            return false;
        }
        
        // Basic world bounds check (if world provides bounds)
        // Additional checks could include obstacle detection, but that's
        // better handled by the pathfinding service
        
        return true;
    }

    private Optional<MovementTarget> resolveCoordinateTarget(String target) {
        Matcher matcher = COORDINATE_PATTERN.matcher(target);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        
        try {
            int x = Integer.parseInt(matcher.group(1));
            int y = Integer.parseInt(matcher.group(2));
            int z = Integer.parseInt(matcher.group(3));
            
            BlockPos blockPos = new BlockPos(x, y, z);
            return Optional.of(new MovementTarget(blockPos));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private Optional<MovementTarget> resolveRelativeTarget(String target, Vec3d origin) {
        Matcher matcher = RELATIVE_PATTERN.matcher(target);
        if (!matcher.matches()) {
            return Optional.empty();
        }
        
        String direction = matcher.group(1);
        int distance;
        
        try {
            distance = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
        
        // Validate distance
        if (distance <= 0 || distance > MAX_MOVEMENT_DISTANCE) {
            return Optional.empty();
        }
        
        Vec3d targetPosition = calculateRelativePosition(origin, direction, distance);
        return Optional.of(new MovementTarget(targetPosition));
    }

    private Vec3d calculateRelativePosition(Vec3d origin, String direction, int distance) {
        double x = origin.x;
        double y = origin.y;
        double z = origin.z;
        
        switch (direction) {
            case "north":
                z -= distance;
                break;
            case "south":
                z += distance;
                break;
            case "east":
                x += distance;
                break;
            case "west":
                x -= distance;
                break;
        }
        
        return new Vec3d(x, y, z);
    }

    private Optional<MovementTarget> resolveObjectTarget(String target, World world, Vec3d origin) {
        // Future enhancement: implement object finding logic
        // Could search for specific blocks, entities, or structures
        // For now, return empty to indicate unsupported
        return Optional.empty();
    }
}