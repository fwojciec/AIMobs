package com.aimobs.entity;

import com.aimobs.entity.ai.CommandProcessorService;
import com.aimobs.entity.ai.CommandReceiver;
import com.aimobs.entity.ai.EntityResolverService;
import com.aimobs.entity.ai.GoalService;
import com.aimobs.entity.ai.InteractionService;
import com.aimobs.entity.ai.MovementService;
import com.aimobs.entity.ai.ServiceFactory;
import com.aimobs.entity.ai.TargetResolverService;
import com.aimobs.entity.ai.application.InteractionCommandFactory;
import com.aimobs.entity.ai.application.MovementCommandFactory;
import com.aimobs.entity.ai.core.InventoryActions;
import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.entity.ai.core.AIState;
import com.aimobs.entity.ai.core.CommandExecutor;
import com.aimobs.entity.ai.infrastructure.MinecraftControllableGoal;
import com.aimobs.entity.ai.infrastructure.MoveToLocationGoal;
import com.aimobs.entity.ai.core.EntityId;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtCompound;

import java.util.LinkedList;
import java.util.Queue;

public class AiControlledWolfEntity extends WolfEntity implements CommandExecutor, com.aimobs.entity.ai.core.EntityActions, CommandReceiver {
    
    private static final String AI_ENTITY_ID_KEY = "aiEntityId";
    private static final String AI_CONTROLLED_KEY = "aiControlled";
    
    private EntityId entityId; // Not final since it needs to be restored from NBT
    private final CommandProcessorService commandProcessor;
    private final GoalService goalService;
    private final MovementService movementService;
    private final InteractionService interactionService;
    private final InventoryActions inventoryActions;
    private final EntityResolverService entityResolverService;
    private final MovementCommandFactory movementCommandFactory;
    
    public AiControlledWolfEntity(EntityType<? extends WolfEntity> entityType, World world) {
        this(entityType, world, new LinkedList<>());
    }
    
    // Constructor for dependency injection (testable)
    public AiControlledWolfEntity(EntityType<? extends WolfEntity> entityType, World world, Queue<AICommand> commandQueue) {
        super(entityType, world);
        this.entityId = EntityId.generate(); // Generate unique ID for new entities
        // Composition root pattern - wiring happens here
        this.commandProcessor = ServiceFactory.createCommandProcessor(commandQueue);
        this.goalService = ServiceFactory.createGoalService(this);
        this.movementService = ServiceFactory.createMovementService(this);
        this.interactionService = ServiceFactory.createInteractionService(this, movementService);
        this.inventoryActions = ServiceFactory.createInventoryActions(this);
        this.entityResolverService = ServiceFactory.createEntityResolverService();
        
        TargetResolverService targetResolver = ServiceFactory.createTargetResolverService();
        this.movementCommandFactory = new MovementCommandFactory(movementService, targetResolver, this);
        
        initializeGoals();
    }
    
    // EntityActions implementation - allows dependency injection while maintaining access to protected fields
    @Override
    public void clearGoals() {
        this.goalSelector.clear(goal -> true);
        this.targetSelector.clear(goal -> true);
    }
    
    @Override
    public void addSwimGoal() {
        this.goalSelector.add(1, new SwimGoal(this));
    }
    
    @Override
    public void addEscapeDangerGoal() {
        this.goalSelector.add(2, new EscapeDangerGoal(this, 1.5));
    }
    
    @Override
    public void addControllableGoal() {
        this.goalSelector.add(10, new MinecraftControllableGoal(this));
        // Add movement goal with higher priority than controllable goal
        this.goalSelector.add(5, new MoveToLocationGoal(this, movementService));
    }
    
    public net.minecraft.util.math.Vec3d getPosition() {
        return this.getPos();
    }

    @Override
    public void addInteractionGoal(int priority, Object goal) {
        if (goal instanceof net.minecraft.entity.ai.goal.Goal) {
            this.goalSelector.add(priority, (net.minecraft.entity.ai.goal.Goal) goal);
        }
    }

    @Override
    public void removeInteractionGoal(Object goal) {
        if (goal instanceof net.minecraft.entity.ai.goal.Goal) {
            this.goalSelector.remove((net.minecraft.entity.ai.goal.Goal) goal);
        }
    }

    @Override
    public boolean canReachPosition(net.minecraft.util.math.Vec3d targetPos) {
        return this.getNavigation().findPathTo(targetPos.x, targetPos.y, targetPos.z, 0) != null;
    }

    @Override
    public net.minecraft.world.World getWorld() {
        return super.getWorld();
    }
    
    @Override
    public net.minecraft.entity.passive.WolfEntity getWolfEntity() {
        return this;
    }
    
    private void initializeGoals() {
        goalService.initializeAIGoals();
    }
    
    @Override
    public void executeCommand(AICommand command) {
        commandProcessor.executeCommand(command);
    }
    
    @Override
    public void stopCurrentCommand() {
        commandProcessor.stopCurrentCommand();
    }
    
    @Override
    public AIState getCurrentState() {
        return commandProcessor.getCurrentState();
    }
    
    @Override
    public Queue<AICommand> getCommandQueue() {
        return commandProcessor.getCommandQueue();
    }
    
    @Override
    public void tick() {
        super.tick();
        commandProcessor.tick();
        // Update movement progress each tick
        movementService.updateMovementProgress();
        // Update interaction progress each tick
        interactionService.updateInteractionProgress();
    }
    
    /**
     * Creates a movement command from network message data.
     * This is called by the message parsing system to handle movement commands.
     */
    public AICommand createMovementCommand(com.aimobs.network.core.NetworkMessage message) {
        return movementCommandFactory.createMovementCommand(message, this.getWorld());
    }

    /**
     * Creates an interaction command from network message data.
     * This is called by the message parsing system to handle interaction commands.
     */
    public AICommand createInteractionCommand(com.aimobs.network.core.NetworkMessage message) {
        return InteractionCommandFactory.createInteractionCommand(message, entityResolverService, this.getPos());
    }
    
    // Package-private accessors for testing
    CommandProcessorService getCommandProcessor() {
        return commandProcessor;
    }
    
    GoalService getGoalService() {
        return goalService;
    }

    InteractionService getInteractionService() {
        return interactionService;
    }

    InventoryActions getInventoryActions() {
        return inventoryActions;
    }
    
    // Override to prevent taming mechanics for AI-controlled entities
    @Override
    public boolean canBeLeashedBy(net.minecraft.entity.player.PlayerEntity player) {
        return false;
    }
    
    @Override
    public boolean isBreedingItem(net.minecraft.item.ItemStack stack) {
        return false;
    }

    /**
     * Gets the unique AI entity identifier.
     * Used by persistence system to track entities across save/load cycles.
     */
    public EntityId getAiEntityId() {
        return entityId;
    }

    /**
     * Saves AI-specific data to NBT.
     * Marks this entity as AI-controlled for persistence system.
     */
    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        
        // Mark as AI-controlled entity
        nbt.putBoolean(AI_CONTROLLED_KEY, true);
        
        // Save unique entity ID for persistence tracking
        nbt.putString(AI_ENTITY_ID_KEY, entityId.asString());
    }

    /**
     * Reads AI-specific data from NBT.
     * Restores entity ID for persistence system reconnection.
     */
    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        
        // Restore entity ID from saved data
        if (nbt.contains(AI_ENTITY_ID_KEY)) {
            String savedEntityId = nbt.getString(AI_ENTITY_ID_KEY);
            this.entityId = EntityId.fromString(savedEntityId);
            System.out.println("[AIMobs] Restored EntityId: " + savedEntityId + " for wolf entity");
        } else {
            System.err.println("Warning: AiControlledWolfEntity loaded without saved EntityId - please create a new save");
        }
        
        if (!nbt.getBoolean(AI_CONTROLLED_KEY)) {
            // This shouldn't happen for AI wolves, but handle gracefully
            System.err.println("Warning: AiControlledWolfEntity loaded without AI-controlled flag");
        }
    }
    
    // CommandReceiver interface implementation
    
    @Override
    public EntityId getEntityId() {
        return entityId;
    }
    
    @Override
    public void receiveCommand(AICommand command) {
        if (command != null) {
            getCommandQueue().offer(command);
        }
    }
    
    @Override
    public boolean isAvailable() {
        return isAlive() && !isRemoved();
    }
    
    @Override
    public int getQueuedCommandCount() {
        return getCommandQueue().size();
    }
}