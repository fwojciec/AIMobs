package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.EntityLifecycleService;
import com.aimobs.entity.ai.EntityLookupService;
import com.aimobs.entity.ai.CommandRoutingService;
import com.aimobs.entity.ai.AiPersistenceService;
import com.aimobs.entity.ai.ServiceFactory;
import com.aimobs.AiMobsMod;
import com.aimobs.network.MessageService;
import com.aimobs.network.application.MessageParser;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.server.world.ServerWorld;

/**
 * Infrastructure adapter handling Minecraft world events.
 * Creates persistence services when worlds are loaded and delegates to business logic.
 * 
 * Thin adapter - minimal logic, mostly event forwarding.
 * Infrastructure layer - can depend on all other layers.
 */
public class MinecraftWorldEventHandler {
    
    private EntityLifecycleService lifecycleService;

    public MinecraftWorldEventHandler() {
        // Services will be created when world loads
    }

    /**
     * Registers this handler to listen for world events.
     * Should be called during mod initialization.
     */
    public void register() {
        ServerWorldEvents.LOAD.register(this::onWorldLoad);
    }

    /**
     * Called when a world is loaded.
     * Creates persistence services and triggers reconnection of AI entities.
     * 
     * @param server The minecraft server
     * @param world The world that was loaded
     */
    private void onWorldLoad(net.minecraft.server.MinecraftServer server, ServerWorld world) {
        // Only handle the main world (overworld) to avoid duplicate processing
        if (world.getRegistryKey().equals(net.minecraft.world.World.OVERWORLD)) {
            System.out.println("[AIMobs] World loaded - setting up persistence services...");
            
            try {
                // Create persistence services now that we have a world
                AiPersistenceService persistenceService = ServiceFactory.createAiPersistenceService(world);
                lifecycleService = ServiceFactory.createEntityLifecycleService(persistenceService);
                
                // Reconnect AI entities
                lifecycleService.reconnectAiEntities();
                
                // Create entity lookup and command routing services
                EntityLookupService entityLookup = ServiceFactory.createEntityLookupService(world);
                CommandRoutingService commandRouter = ServiceFactory.createCommandRoutingService(entityLookup);
                
                // Configure MessageParser with the clean command router
                try {
                    MessageService messageService = AiMobsMod.getMessageService();
                    if (messageService instanceof MessageParser parser) {
                        parser.setCommandRouter(commandRouter);
                        System.out.println("[AIMobs] MessageParser configured with clean CommandRouter");
                    }
                    System.out.println("[AIMobs] Command routing services created successfully");
                } catch (Exception e) {
                    System.err.println("[AIMobs] Error configuring MessageParser: " + e.getMessage());
                }
                
                System.out.println("[AIMobs] AI entities reconnected successfully");
            } catch (Exception e) {
                System.err.println("[AIMobs] Error setting up persistence services: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}