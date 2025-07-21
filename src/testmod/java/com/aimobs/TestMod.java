package com.aimobs;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("aimobs-testmod");
    
    @Override
    public void onInitialize() {
        LOGGER.info("AI Mobs Controller Test Mod initialized successfully!");
    }
}