package com.aimobs.entity.ai;

import com.aimobs.entity.ai.application.CommandProcessor;
import com.aimobs.entity.ai.application.EntityResolver;
import com.aimobs.entity.ai.application.GoalCoordinator;
import com.aimobs.entity.ai.application.InteractionCoordinator;
import com.aimobs.entity.ai.application.MovementCoordinator;
import com.aimobs.entity.ai.application.TargetResolver;
import com.aimobs.entity.ai.application.WolfInventoryManager;
import com.aimobs.entity.ai.application.EntityLifecycleCoordinator;
import com.aimobs.entity.ai.application.CommandRouter;
import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.entity.ai.core.EntityActions;
import com.aimobs.entity.ai.core.InventoryActions;
import com.aimobs.entity.ai.infrastructure.MinecraftPathfindingService;
import com.aimobs.entity.ai.infrastructure.MinecraftAiPersistenceAdapter;
import com.aimobs.entity.ai.infrastructure.MinecraftWorldEventHandler;
import com.aimobs.entity.ai.infrastructure.MinecraftEntityLookupService;
import com.aimobs.network.MessageService;
import com.aimobs.network.WebSocketService;
import com.aimobs.network.application.MessageParser;
import com.aimobs.network.application.NetworkCommandCoordinator;
import com.aimobs.network.application.TestableWebSocketService;
import net.minecraft.entity.passive.WolfEntity;

import java.util.Queue;

/**
 * Service Factory - Composition Root for dependency injection.
 * 
 * Following standard package layout principles:
 * - Root package coordinates construction
 * - Returns interfaces, constructs concrete implementations
 * - No dependencies flow between peer packages
 * - All wiring happens here
 */
public class ServiceFactory {
    
    /**
     * Create command processor service with injected dependencies.
     * Returns interface, constructs concrete implementation.
     */
    public static CommandProcessorService createCommandProcessor(Queue<AICommand> commandQueue) {
        return new CommandProcessor(commandQueue);
    }
    
    /**
     * Create goal service with injected dependencies.
     * Returns interface, constructs concrete implementation.
     */
    public static GoalService createGoalService(EntityActions entityActions) {
        return new GoalCoordinator(entityActions);
    }
    
    /**
     * Create message service with injected dependencies.
     * Returns interface, constructs concrete implementation.
     */
    public static MessageService createMessageService(CommandProcessorService commandProcessor, CommandRoutingService commandRouter) {
        return new MessageParser(commandProcessor, commandRouter);
    }
    
    /**
     * Create message service with command processor only (legacy support).
     * Creates a null command router - for backward compatibility during transition.
     */
    public static MessageService createMessageService(CommandProcessorService commandProcessor) {
        // Create a null command router for legacy support
        // This will be removed once all callers are updated
        return new MessageParser(commandProcessor, null);
    }
    
    /**
     * Create WebSocket service with injected dependencies.
     * Returns interface, constructs concrete implementation.
     * Uses seam pattern for testability.
     */
    public static WebSocketService createWebSocketService(MessageService messageService) {
        NetworkCommandCoordinator coordinator = new NetworkCommandCoordinator(messageService);
        
        // In production, use real WebSocket connection
        com.aimobs.network.core.WebSocketConnection connection = 
            new com.aimobs.network.infrastructure.JavaWebSocketConnection();
        
        return new TestableWebSocketService(connection, coordinator);
    }
    
    /**
     * Create testable WebSocket service for testing.
     * This is our seam - we can substitute test doubles.
     */
    public static WebSocketService createTestableWebSocketService(
            com.aimobs.network.core.WebSocketConnection connection, 
            MessageService messageService) {
        NetworkCommandCoordinator coordinator = new NetworkCommandCoordinator(messageService);
        return new TestableWebSocketService(connection, coordinator);
    }
    
    /**
     * Create movement service with injected dependencies.
     * Returns interface, constructs concrete implementation.
     */
    public static MovementService createMovementService(WolfEntity wolfEntity) {
        // Cast to AiControlledWolfEntity which implements EntityActions
        if (!(wolfEntity instanceof com.aimobs.entity.AiControlledWolfEntity)) {
            throw new IllegalArgumentException("Wolf entity must be an AiControlledWolfEntity");
        }
        EntityActions entityActions = (EntityActions) wolfEntity;
        PathfindingService pathfindingService = new MinecraftPathfindingService(wolfEntity);
        return new MovementCoordinator(entityActions, pathfindingService);
    }
    
    /**
     * Create target resolver service.
     * Returns interface, constructs concrete implementation.
     */
    public static TargetResolverService createTargetResolverService() {
        return new TargetResolver();
    }
    
    /**
     * Create pathfinding service with injected dependencies.
     * Returns interface, constructs concrete implementation.
     */
    public static PathfindingService createPathfindingService(WolfEntity wolfEntity) {
        return new MinecraftPathfindingService(wolfEntity);
    }
    
    /**
     * Create testable movement service for testing.
     * This is our seam - we can substitute test doubles.
     */
    public static MovementService createTestableMovementService(
            EntityActions entityActions, 
            PathfindingService pathfindingService) {
        return new MovementCoordinator(entityActions, pathfindingService);
    }
    
    /**
     * Create interaction service with injected dependencies.
     * Returns interface, constructs concrete implementation.
     */
    public static InteractionService createInteractionService(WolfEntity wolfEntity, MovementService movementService) {
        // Cast to AiControlledWolfEntity which implements EntityActions
        if (!(wolfEntity instanceof com.aimobs.entity.AiControlledWolfEntity)) {
            throw new IllegalArgumentException("Wolf entity must be an AiControlledWolfEntity");
        }
        EntityActions entityActions = (EntityActions) wolfEntity;
        InventoryActions inventoryActions = new WolfInventoryManager(wolfEntity);
        return new InteractionCoordinator(entityActions, inventoryActions, movementService);
    }
    
    /**
     * Create inventory manager for wolf entities.
     * Returns interface, constructs concrete implementation.
     */
    public static InventoryActions createInventoryActions(WolfEntity wolfEntity) {
        return new WolfInventoryManager(wolfEntity);
    }
    
    /**
     * Create testable interaction service for testing.
     * This is our seam - we can substitute test doubles.
     */
    public static InteractionService createTestableInteractionService(
            EntityActions entityActions,
            InventoryActions inventoryActions,
            MovementService movementService) {
        return new InteractionCoordinator(entityActions, inventoryActions, movementService);
    }
    
    /**
     * Create entity resolver service.
     * Returns interface, constructs concrete implementation.
     */
    public static EntityResolverService createEntityResolverService() {
        return new EntityResolver();
    }
    
    /**
     * Create testable entity resolver service for testing.
     * This is our seam - we can substitute test doubles.
     */
    public static EntityResolverService createTestableEntityResolverService() {
        // Can return fake implementation for testing
        return new EntityResolver();
    }

    /**
     * Create AI persistence service with injected dependencies.
     * Returns interface, constructs concrete implementation.
     */
    public static AiPersistenceService createAiPersistenceService(net.minecraft.server.world.ServerWorld world) {
        return new MinecraftAiPersistenceAdapter(world);
    }

    /**
     * Create entity lifecycle service with injected dependencies.
     * Returns interface, constructs concrete implementation.
     */
    public static EntityLifecycleService createEntityLifecycleService(AiPersistenceService persistenceService) {
        return new EntityLifecycleCoordinator(persistenceService);
    }

    /**
     * Create world event handler.
     * Returns concrete implementation for infrastructure layer.
     * Services are created when world loads.
     */
    public static MinecraftWorldEventHandler createWorldEventHandler() {
        return new MinecraftWorldEventHandler();
    }

    /**
     * Create testable entity lifecycle service for testing.
     * This is our seam - we can substitute test doubles.
     */
    public static EntityLifecycleService createTestableEntityLifecycleService(AiPersistenceService persistenceService) {
        return new EntityLifecycleCoordinator(persistenceService);
    }

    /**
     * Create entity lookup service with world context.
     * Returns interface, constructs concrete implementation.
     */
    public static EntityLookupService createEntityLookupService(net.minecraft.server.world.ServerWorld world) {
        return new MinecraftEntityLookupService(world);
    }

    /**
     * Create command routing service with entity lookup.
     * Returns interface, constructs concrete implementation.
     */
    public static CommandRoutingService createCommandRoutingService(EntityLookupService entityLookup) {
        return new CommandRouter(entityLookup);
    }

    /**
     * Create testable entity lookup service for testing.
     * This is our seam - we can substitute test doubles.
     */
    public static EntityLookupService createTestableEntityLookupService() {
        // Can return fake implementation for testing
        return null; // TODO: Implement FakeEntityLookupService
    }

    /**
     * Create testable command routing service for testing.
     * This is our seam - we can substitute test doubles.
     */
    public static CommandRoutingService createTestableCommandRoutingService(EntityLookupService entityLookup) {
        return new CommandRouter(entityLookup);
    }
}