package com.aimobs.command;

import com.aimobs.AiMobsMod;
import com.aimobs.entity.AiControlledWolfEntity;
import com.aimobs.entity.ModEntities;
import com.aimobs.entity.ai.EntityLifecycleService;
import com.aimobs.entity.ai.ServiceFactory;
import com.aimobs.entity.ai.AiPersistenceService;
import com.aimobs.network.MessageService;
import com.aimobs.network.application.MessageParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class SpawnAiWolfCommand {
    
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("spawn_ai_wolf")
            .requires(source -> source.hasPermissionLevel(0)) // Allow all players in single-player
            .executes(SpawnAiWolfCommand::execute));
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        try {
            ServerCommandSource source = context.getSource();
            ServerWorld world = source.getWorld();
            Vec3d position = source.getPosition();
            
            AiMobsMod.LOGGER.info("Attempting to spawn AI wolf at {}, {}, {}", position.x, position.y, position.z);
            
            AiControlledWolfEntity wolf = new AiControlledWolfEntity(ModEntities.AI_CONTROLLED_WOLF, world);
            wolf.refreshPositionAndAngles(position.x, position.y, position.z, 0, 0);
            
            AiMobsMod.LOGGER.info("Created wolf entity, attempting to spawn in world");
            
            if (world.spawnEntity(wolf)) {
                // Register the entity with the lifecycle service for persistence
                try {
                    AiPersistenceService persistenceService = ServiceFactory.createAiPersistenceService(world);
                    EntityLifecycleService lifecycleService = ServiceFactory.createEntityLifecycleService(persistenceService);
                    lifecycleService.registerAiEntity(wolf.getAiEntityId());
                    AiMobsMod.LOGGER.info("Registered AI wolf with lifecycle service for persistence");
                } catch (Exception e) {
                    AiMobsMod.LOGGER.warn("Failed to register wolf with lifecycle service: {}", e.getMessage());
                    // Continue anyway - the wolf can still be spawned
                }
                
                // Wolf will be automatically discovered by the command routing system
                AiMobsMod.LOGGER.info("AI wolf spawned - will be available for commands through clean architecture");
                
                source.sendFeedback(() -> Text.literal("Spawned AI-controlled wolf at " + 
                    String.format("%.1f, %.1f, %.1f", position.x, position.y, position.z)), true);
                AiMobsMod.LOGGER.info("Successfully spawned AI wolf");
                return 1;
            } else {
                AiMobsMod.LOGGER.error("Failed to spawn wolf entity in world");
                source.sendFeedback(() -> Text.literal("Failed to spawn AI-controlled wolf"), false);
                return 0;
            }
        } catch (Exception e) {
            AiMobsMod.LOGGER.error("Error executing spawn command", e);
            context.getSource().sendFeedback(() -> Text.literal("Error spawning wolf: " + e.getMessage()), false);
            return 0;
        }
    }
}