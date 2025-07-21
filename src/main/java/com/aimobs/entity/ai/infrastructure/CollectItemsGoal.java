package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.InteractionService;
import com.aimobs.entity.ai.core.CollectItemsCommand;
import com.aimobs.entity.ai.core.GameItem;
import com.aimobs.entity.ai.core.InventoryActions;
import com.aimobs.entity.ai.infrastructure.MinecraftItemWrapper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Minecraft AI Goal for collecting items within a specified area.
 * Infrastructure layer - platform-specific implementation.
 * 
 * Following Ben Johnson's standard package layout:
 * - Infrastructure layer adapts external systems to internal interfaces
 * - Can depend on all other layers
 */
public class CollectItemsGoal extends Goal {
    
    private final WolfEntity wolf;
    private final InteractionService interactionService;
    private final InventoryActions inventoryActions;
    private final CollectItemsCommand command;
    private ItemEntity currentTarget;
    private final double pickupRange = 1.5;
    
    public CollectItemsGoal(WolfEntity wolf, InteractionService interactionService, 
                           InventoryActions inventoryActions, CollectItemsCommand command) {
        this.wolf = wolf;
        this.interactionService = interactionService;
        this.inventoryActions = inventoryActions;
        this.command = command;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }
    
    @Override
    public boolean canStart() {
        if (command.isComplete() || command.isCancelled()) {
            return false;
        }
        
        // Check if we've reached the item limit
        if (command.getMaxItems() > 0 && 
            command.getItemsCollected() >= command.getMaxItems()) {
            return false;
        }
        
        // Look for items to collect
        findNextTarget();
        return currentTarget != null;
    }
    
    @Override
    public boolean shouldContinue() {
        if (command.isComplete() || command.isCancelled()) {
            return false;
        }
        
        // Check if we've reached the item limit
        if (command.getMaxItems() > 0 && 
            command.getItemsCollected() >= command.getMaxItems()) {
            return false;
        }
        
        // Continue if we have a valid target or can find one
        if (currentTarget != null && currentTarget.isAlive()) {
            return true;
        }
        
        findNextTarget();
        return currentTarget != null;
    }
    
    @Override
    public void start() {
        // Find initial target
        findNextTarget();
    }
    
    @Override
    public void tick() {
        if (currentTarget == null || !currentTarget.isAlive()) {
            findNextTarget();
            return;
        }
        
        double distance = wolf.distanceTo(currentTarget);
        
        if (distance <= pickupRange) {
            // Close enough to pick up
            tryPickupItem(currentTarget);
            currentTarget = null;
            findNextTarget();
        } else {
            // Move towards the item
            wolf.getNavigation().startMovingTo(currentTarget, 1.0);
            
            // Request positioning assistance if needed
            if (distance > command.getRadius() * 0.8) {
                Vec3d targetPos = currentTarget.getPos();
                interactionService.coordinatePositioning(targetPos);
            }
        }
        
        // Update interaction progress
        interactionService.updateInteractionProgress();
    }
    
    @Override
    public void stop() {
        wolf.getNavigation().stop();
        currentTarget = null;
    }
    
    @Override
    public boolean canStop() {
        // Allow higher priority goals to interrupt
        return true;
    }
    
    private void findNextTarget() {
        Vec3d wolfPos = wolf.getPos();
        double radius = command.getRadius();
        
        Box searchBox = new Box(
            wolfPos.x - radius, wolfPos.y - radius, wolfPos.z - radius,
            wolfPos.x + radius, wolfPos.y + radius, wolfPos.z + radius
        );
        
        List<ItemEntity> nearbyItems = wolf.getWorld()
            .getEntitiesByClass(ItemEntity.class, searchBox, this::isValidItem)
            .stream()
            .sorted((a, b) -> Double.compare(wolf.distanceTo(a), wolf.distanceTo(b)))
            .collect(Collectors.toList());
        
        currentTarget = nearbyItems.isEmpty() ? null : nearbyItems.get(0);
    }
    
    private boolean isValidItem(ItemEntity itemEntity) {
        if (itemEntity == null || !itemEntity.isAlive()) {
            return false;
        }
        
        ItemStack itemStack = itemEntity.getStack();
        String itemType = command.getItemType();
        
        // Check if item type matches filter
        if ("all".equals(itemType)) {
            return true;
        }
        
        String itemName = itemStack.getItem().toString().toLowerCase();
        return itemName.contains(itemType.toLowerCase());
    }
    
    private void tryPickupItem(ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getStack();
        
        // Convert ItemStack to GameItem for clean architecture
        GameItem gameItem = new MinecraftItemWrapper(itemStack);
        
        if (inventoryActions.hasSpace(gameItem)) {
            if (inventoryActions.addItem(gameItem)) {
                command.incrementItemsCollected();
                itemEntity.discard();
            }
        }
    }
    
    /**
     * @return The collect command this goal is executing
     */
    public CollectItemsCommand getCommand() {
        return command;
    }
    
    /**
     * @return The current item target, or null if none
     */
    public ItemEntity getCurrentTarget() {
        return currentTarget;
    }
}