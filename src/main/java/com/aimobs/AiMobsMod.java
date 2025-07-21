package com.aimobs;

import com.aimobs.command.SpawnAiWolfCommand;
import com.aimobs.core.exceptions.ConfigurationException;
import com.aimobs.core.exceptions.NetworkException;
import com.aimobs.entity.ModEntities;
import com.aimobs.entity.ai.CommandProcessorService;
import com.aimobs.entity.ai.ServiceFactory;
import com.aimobs.entity.ai.EntityLifecycleService;
import com.aimobs.entity.ai.AiPersistenceService;
import com.aimobs.entity.ai.infrastructure.MinecraftWorldEventHandler;
import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.network.MessageService;
import com.aimobs.network.WebSocketService;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

public class AiMobsMod implements ModInitializer {
    public static final String MOD_ID = "aimobs";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static WebSocketService webSocketService;
    private static MessageService messageService;
    private static CommandProcessorService commandProcessor;
    private static MinecraftWorldEventHandler worldEventHandler;
    
    @Override
    public void onInitialize() {
        ModEntities.registerEntities();
        
        // Initialize WebSocket networking services
        initializeNetworkServices();
        
        // Initialize persistence services
        initializePersistenceServices();
        
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SpawnAiWolfCommand.register(dispatcher, registryAccess);
        });
        
        LOGGER.info("AI Mobs Controller initialized successfully!");
    }
    
    private void initializeNetworkServices() {
        try {
            // Create command processor with queue
            commandProcessor = ServiceFactory.createCommandProcessor(new ConcurrentLinkedQueue<AICommand>());
            
            // Create message service
            messageService = ServiceFactory.createMessageService(commandProcessor);
            
            // Create WebSocket service
            webSocketService = ServiceFactory.createWebSocketService(messageService);
            
            // Connect to WebSocket server (configurable URL)
            String serverUrl = System.getProperty("aimobs.websocket.url", "ws://localhost:8080");
            webSocketService.connect(serverUrl);
            
            LOGGER.info("WebSocket services initialized. Connecting to: " + serverUrl);
            
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid service configuration during initialization", e);
        } catch (NullPointerException e) {
            LOGGER.error("Missing dependency during service initialization", e);
        } catch (RuntimeException e) {
            LOGGER.error("Unexpected error during service initialization", e);
        }
    }
    
    private void initializePersistenceServices() {
        try {
            // Register world event handler that will create proper persistence services
            // when worlds are loaded (since we need ServerWorld for persistence)
            worldEventHandler = ServiceFactory.createWorldEventHandler();
            worldEventHandler.register();
            
            LOGGER.info("World event handler registered for persistence services");
            
        } catch (Exception e) {
            LOGGER.error("Error registering world event handler", e);
        }
    }
    
    public static WebSocketService getWebSocketService() {
        return webSocketService;
    }
    
    public static MessageService getMessageService() {
        return messageService;
    }
    
    public static CommandProcessorService getCommandProcessor() {
        return commandProcessor;
    }
    
    public static void shutdown() {
        if (webSocketService != null) {
            webSocketService.shutdown();
            LOGGER.info("WebSocket services shutdown completed");
        }
    }
}