package com.aimobs;

import com.aimobs.entity.ModEntities;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.WolfEntityRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiMobsClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("aimobs-client");
    
    @Override
    public void onInitializeClient() {
        // Register entity renderer for AI controlled wolf
        EntityRendererRegistry.register(ModEntities.AI_CONTROLLED_WOLF, WolfEntityRenderer::new);
        
        LOGGER.info("AI Mobs Controller client initialized successfully!");
        LOGGER.info("Registered renderer for AI controlled wolf");
    }
}