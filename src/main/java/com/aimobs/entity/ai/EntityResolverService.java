package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.TargetEntity;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.AiControlledWolfEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Service contract for resolving string-based entity targets to actual entities.
 * Handles various target formats for interaction commands.
 * 
 * Following Ben Johnson's standard package layout:
 * - Root interface defining service contract
 * - No implementation details - only behavior contracts
 */
public interface EntityResolverService {
    
    /**
     * Resolves a string target to a concrete entity for interaction.
     * 
     * Supported formats:
     * - Entity types: "zombie", "skeleton", "spider", "creeper"
     * - Generic: "hostile", "enemy" (finds nearest hostile)
     * - Specific names: future enhancement
     * 
     * @param targetType The target type string to resolve
     * @param world The world context for entity lookup
     * @param origin The origin position for distance calculations
     * @param maxDistance Maximum distance to search for entities
     * @return Optional containing the resolved entity, or empty if none found
     */
    Optional<TargetEntity> resolveEntity(String targetType, World world, Vec3d origin, double maxDistance);
    
    /**
     * Validates if a target type string is potentially resolvable.
     * This is a quick check that doesn't require world access.
     * 
     * @param targetType The target type string to validate
     * @return true if the target type format is recognized
     */
    boolean isValidEntityType(String targetType);
    
    /**
     * Checks if an entity is a valid target for interactions.
     * Considers entity type, state, and interaction safety rules.
     * 
     * @param entity The entity to check
     * @return true if the entity can be safely targeted
     */
    boolean isValidInteractionTarget(TargetEntity entity);
    
    /**
     * Finds an AI-controlled wolf entity by its unique EntityId.
     * Used for reconnecting commands to specific AI entities after world reload.
     * 
     * @param entityId The unique identifier of the AI entity
     * @param world The world context for entity lookup
     * @return Optional containing the wolf entity, or empty if not found
     */
    Optional<AiControlledWolfEntity> findAiWolfById(EntityId entityId, World world);
    
    /**
     * Finds any available AI-controlled wolf entity for command processing.
     * Returns the first available wolf if multiple exist.
     * 
     * @param world The world context for entity lookup
     * @return Optional containing an available wolf entity, or empty if none found
     */
    Optional<AiControlledWolfEntity> findAnyAvailableWolf(World world);
}