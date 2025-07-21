package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.MovementService;
import com.aimobs.entity.ai.TargetResolverService;
import com.aimobs.entity.ai.core.*;
import com.aimobs.network.core.NetworkMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating movement commands from network messages.
 * Handles the conversion from WebSocket commands to movement AICommand implementations.
 */
public class MovementCommandFactory {
    
    private final MovementService movementService;
    private final TargetResolverService targetResolver;
    private final EntityActions entityActions;

    public MovementCommandFactory(MovementService movementService, 
                                TargetResolverService targetResolver,
                                EntityActions entityActions) {
        this.movementService = movementService;
        this.targetResolver = targetResolver;
        this.entityActions = entityActions;
    }

    /**
     * Creates a movement command from a network message.
     * 
     * @param message The network message containing movement instructions
     * @param world The world context for target resolution
     * @return AICommand for the movement, or null if invalid
     */
    public AICommand createMovementCommand(NetworkMessage message, World world) {
        if (message == null || message.getData() == null) {
            return null;
        }

        String action = message.getData().getAction();
        Map<String, Object> parameters = message.getData().getParameters();

        switch (action) {
            case "move":
                return createMoveCommand(parameters, world);
            case "follow":
                return createFollowCommand(parameters, world);
            case "stop":
                return createStopCommand();
            case "comeHere":
                return createComeHereCommand(parameters, world);
            default:
                return null;
        }
    }

    private AICommand createMoveCommand(Map<String, Object> parameters, World world) {
        MovementTarget target = resolveMovementTarget(parameters, world);
        if (target == null) {
            return null;
        }
        return new MoveToLocationCommand(movementService, target);
    }

    private AICommand createFollowCommand(Map<String, Object> parameters, World world) {
        PlayerEntity player = findPlayer(parameters, world);
        if (player == null) {
            return null;
        }
        return new FollowPlayerCommand(movementService, player);
    }

    private AICommand createStopCommand() {
        return new StopMovementCommand(movementService);
    }

    private AICommand createComeHereCommand(Map<String, Object> parameters, World world) {
        PlayerEntity player = findPlayer(parameters, world);
        if (player == null) {
            return null;
        }
        return new ComeHereCommand(movementService, player);
    }

    private MovementTarget resolveMovementTarget(Map<String, Object> parameters, World world) {
        // Try coordinate-based target first
        if (parameters.containsKey("x") && parameters.containsKey("y") && parameters.containsKey("z")) {
            try {
                int x = ((Number) parameters.get("x")).intValue();
                int y = ((Number) parameters.get("y")).intValue();
                int z = ((Number) parameters.get("z")).intValue();
                return new MovementTarget(new BlockPos(x, y, z));
            } catch (ClassCastException | NullPointerException e) {
                // Invalid coordinates
                return null;
            }
        }

        // Try string-based target resolution
        Object targetParam = parameters.get("target");
        if (targetParam instanceof String) {
            String targetString = (String) targetParam;
            Vec3d origin = entityActions.getPosition();
            Optional<MovementTarget> resolved = targetResolver.resolveTarget(targetString, world, origin);
            return resolved.orElse(null);
        }

        return null;
    }

    private PlayerEntity findPlayer(Map<String, Object> parameters, World world) {
        // For now, find the closest player
        // In a more advanced implementation, this could use player names or IDs
        Vec3d entityPosition = entityActions.getPosition();
        return world.getClosestPlayer(entityPosition.x, entityPosition.y, entityPosition.z, 50.0, false);
    }
}