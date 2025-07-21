package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.EntityResolverService;
import com.aimobs.entity.ai.core.*;
import com.aimobs.network.core.NetworkMessage;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.Optional;

/**
 * Factory for creating interaction commands from network messages.
 * Application layer - contains business logic for command creation.
 * 
 * Following Ben Johnson's standard package layout:
 * - Application layer implements business logic
 * - Depends on core layer and root interfaces only
 */
public class InteractionCommandFactory {
    
    /**
     * Creates an interaction command from a network message.
     * 
     * @param message The network message containing command data
     * @param entityResolver The entity resolver service for finding targets
     * @param entityPos The position of the entity issuing the command
     * @return An InteractionCommand, or null if the message cannot be processed
     */
    public static InteractionCommand createInteractionCommand(NetworkMessage message, EntityResolverService entityResolver, Vec3d entityPos) {
        if (!isValidInteractionMessage(message)) {
            return null;
        }
        
        String action = message.getData().getAction();
        Map<String, Object> parameters = message.getData().getParameters();
        
        switch (action.toLowerCase()) {
            case "attack":
                return createAttackCommand(parameters, entityResolver, entityPos);
            case "collect":
                return createCollectCommand(parameters);
            case "defend":
                return createDefendCommand(parameters, entityPos);
            case "speak":
            case "communicate":
                return createCommunicationCommand(parameters);
            default:
                return null;
        }
    }
    
    private static boolean isValidInteractionMessage(NetworkMessage message) {
        return message != null &&
               message.getData() != null &&
               message.getData().getAction() != null &&
               message.getData().getParameters() != null;
    }
    
    private static AttackTargetCommand createAttackCommand(Map<String, Object> parameters, EntityResolverService entityResolver, Vec3d entityPos) {
        Object targetParam = parameters.get("target");
        if (targetParam == null) {
            return null;
        }
        
        String targetType = targetParam.toString().toLowerCase();
        Optional<com.aimobs.entity.ai.core.TargetEntity> targetOpt = entityResolver.resolveEntity(targetType, null, entityPos, 16.0);
        
        if (targetOpt.isEmpty()) {
            return null;
        }
        
        // Get priority if specified
        int priority = InteractionType.ATTACK.getDefaultPriority();
        Object priorityParam = parameters.get("priority");
        if (priorityParam instanceof Number) {
            priority = ((Number) priorityParam).intValue();
        }
        
        return new AttackTargetCommand(targetOpt.get(), priority);
    }
    
    private static CollectItemsCommand createCollectCommand(Map<String, Object> parameters) {
        Object itemParam = parameters.get("item");
        if (itemParam == null) {
            return null;
        }
        
        String itemType = itemParam.toString();
        
        // Get radius (default: 10 blocks)
        double radius = 10.0;
        Object radiusParam = parameters.get("radius");
        if (radiusParam instanceof Number) {
            radius = ((Number) radiusParam).doubleValue();
        }
        
        // Get max items (default: 0 = unlimited)
        int maxItems = 0;
        Object maxParam = parameters.get("max_items");
        if (maxParam instanceof Number) {
            maxItems = ((Number) maxParam).intValue();
        }
        
        // Get priority if specified
        int priority = InteractionType.COLLECT.getDefaultPriority();
        Object priorityParam = parameters.get("priority");
        if (priorityParam instanceof Number) {
            priority = ((Number) priorityParam).intValue();
        }
        
        return new CollectItemsCommand(itemType, radius, maxItems, priority);
    }
    
    private static DefendAreaCommand createDefendCommand(Map<String, Object> parameters, Vec3d entityPos) {
        // Determine the area to defend
        BlockPos centerPos = null;
        Object areaParam = parameters.get("area");
        
        if ("here".equals(areaParam) || areaParam == null) {
            // Defend current location
            centerPos = new BlockPos((int)entityPos.x, (int)entityPos.y, (int)entityPos.z);
        } else if (areaParam instanceof Map) {
            // Defend specific coordinates
            @SuppressWarnings("unchecked")
            Map<String, Object> coords = (Map<String, Object>) areaParam;
            Object x = coords.get("x");
            Object y = coords.get("y");
            Object z = coords.get("z");
            
            if (x instanceof Number && y instanceof Number && z instanceof Number) {
                centerPos = new BlockPos(
                    ((Number) x).intValue(),
                    ((Number) y).intValue(),
                    ((Number) z).intValue()
                );
            }
        }
        
        if (centerPos == null) {
            return null;
        }
        
        // Get radius (default: 15 blocks)
        double radius = 15.0;
        Object radiusParam = parameters.get("radius");
        if (radiusParam instanceof Number) {
            radius = ((Number) radiusParam).doubleValue();
        }
        
        // Get priority if specified
        int priority = InteractionType.DEFEND.getDefaultPriority();
        Object priorityParam = parameters.get("priority");
        if (priorityParam instanceof Number) {
            priority = ((Number) priorityParam).intValue();
        }
        
        return new DefendAreaCommand(centerPos, radius, priority);
    }
    
    private static CommunicationCommand createCommunicationCommand(Map<String, Object> parameters) {
        Object messageParam = parameters.get("message");
        if (messageParam == null) {
            return null;
        }
        
        String message = messageParam.toString();
        
        // Get priority if specified
        int priority = InteractionType.COMMUNICATE.getDefaultPriority();
        Object priorityParam = parameters.get("priority");
        if (priorityParam instanceof Number) {
            priority = ((Number) priorityParam).intValue();
        }
        
        return new CommunicationCommand(message, priority);
    }
}