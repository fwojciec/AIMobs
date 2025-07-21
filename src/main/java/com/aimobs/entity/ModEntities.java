package com.aimobs.entity;

import com.aimobs.AiMobsMod;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    
    public static final EntityType<AiControlledWolfEntity> AI_CONTROLLED_WOLF = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier(AiMobsMod.MOD_ID, "ai_controlled_wolf"),
        FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, 
                (EntityType<AiControlledWolfEntity> type, net.minecraft.world.World world) -> 
                    new AiControlledWolfEntity(type, world))
            .dimensions(EntityDimensions.fixed(0.6f, 0.85f))
            .build()
    );
    
    public static void registerEntities() {
        AiMobsMod.LOGGER.info("Registering entities for " + AiMobsMod.MOD_ID);
        
        // Register entity attributes - this is crucial for custom entities
        FabricDefaultAttributeRegistry.register(AI_CONTROLLED_WOLF, WolfEntity.createWolfAttributes());
        AiMobsMod.LOGGER.info("Registered attributes for AI controlled wolf");
    }
}