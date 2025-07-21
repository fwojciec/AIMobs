package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.core.GameItem;
import com.aimobs.entity.ai.core.InventoryActions;
import com.aimobs.entity.ai.core.ItemType;
import com.aimobs.entity.ai.infrastructure.MinecraftItemWrapper;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Application layer implementation of InventoryActions for Wolf entities.
 * Contains business logic for inventory management.
 * 
 * Following Ben Johnson's standard package layout:
 * - Application layer implements service contracts
 * - Contains business logic but no infrastructure concerns
 * - Manages wolf-specific inventory behavior
 */
public class WolfInventoryManager implements InventoryActions {
    
    private final WolfEntity wolf;
    private final List<GameItem> inventory;
    private final int maxCapacity;
    
    public WolfInventoryManager(WolfEntity wolf) {
        this(wolf, 27); // Default capacity similar to a chest
    }
    
    public WolfInventoryManager(WolfEntity wolf, int maxCapacity) {
        this.wolf = wolf;
        this.maxCapacity = maxCapacity;
        this.inventory = new ArrayList<>();
    }
    
    @Override
    public boolean addItem(GameItem item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        
        if (!hasSpace(item)) {
            return false;
        }
        
        // Create a working copy to avoid modifying the original
        GameItem workingItem = item.withCount(item.getCount());
        
        // Try to stack with existing items first
        for (GameItem existingItem : inventory) {
            if (existingItem.canCombineWith(workingItem)) {
                int availableSpace = existingItem.getMaxStackSize() - existingItem.getCount();
                int toAdd = Math.min(availableSpace, workingItem.getCount());
                
                existingItem.setCount(existingItem.getCount() + toAdd);
                workingItem.setCount(workingItem.getCount() - toAdd);
                
                if (workingItem.isEmpty()) {
                    return true;
                }
            }
        }
        
        // Add remaining items as new stacks
        while (!workingItem.isEmpty() && inventory.size() < maxCapacity) {
            int stackSize = Math.min(workingItem.getCount(), workingItem.getMaxStackSize());
            GameItem newStack = workingItem.withCount(stackSize);
            
            inventory.add(newStack);
            workingItem.setCount(workingItem.getCount() - stackSize);
        }
        
        return workingItem.isEmpty();
    }
    
    @Override
    public int removeItem(ItemType itemType, int count) {
        if (itemType == null || count <= 0) {
            return 0;
        }
        
        int removed = 0;
        List<GameItem> toRemove = new ArrayList<>();
        
        for (GameItem item : inventory) {
            if (item.getItemType().equals(itemType.getMinecraftId())) {
                int toTake = Math.min(count - removed, item.getCount());
                item.setCount(item.getCount() - toTake);
                removed += toTake;
                
                if (item.isEmpty()) {
                    toRemove.add(item);
                }
                
                if (removed >= count) {
                    break;
                }
            }
        }
        
        inventory.removeAll(toRemove);
        return removed;
    }
    
    @Override
    public int getItemCount(ItemType itemType) {
        if (itemType == null) {
            return 0;
        }
        
        return inventory.stream()
            .filter(item -> item.getItemType().equals(itemType.getMinecraftId()))
            .mapToInt(GameItem::getCount)
            .sum();
    }
    
    @Override
    public List<GameItem> getAllItems() {
        return new ArrayList<>(inventory);
    }
    
    @Override
    public boolean hasSpace(GameItem item) {
        if (item == null || item.isEmpty()) {
            return true;
        }
        
        int remainingToAdd = item.getCount();
        
        // Check if we can stack with existing items
        for (GameItem existingItem : inventory) {
            if (existingItem.canCombineWith(item)) {
                int availableSpace = existingItem.getMaxStackSize() - existingItem.getCount();
                remainingToAdd -= availableSpace;
                
                if (remainingToAdd <= 0) {
                    return true;
                }
            }
        }
        
        // Check if we have empty slots for remaining items
        int emptySlots = maxCapacity - inventory.size();
        int stacksNeeded = (int) Math.ceil((double) remainingToAdd / item.getMaxStackSize());
        
        return stacksNeeded <= emptySlots;
    }
    
    @Override
    public int getItemCount() {
        return inventory.size();
    }
    
    @Override
    public int getMaxCapacity() {
        return maxCapacity;
    }
    
    @Override
    public void clearInventory() {
        inventory.clear();
    }
    
    @Override
    public boolean dropItem(GameItem item) {
        if (item == null || item.isEmpty() || wolf.getWorld().isClient) {
            return false;
        }
        
        // Convert GameItem to ItemStack for Minecraft dropping
        ItemStack itemStack = convertToItemStack(item);
        if (itemStack.isEmpty()) {
            return false;
        }
        
        Vec3d pos = wolf.getPos();
        ItemEntity itemEntity = new ItemEntity(
            wolf.getWorld(),
            pos.x,
            pos.y + 0.5,
            pos.z,
            itemStack
        );
        
        // Add some random velocity to the dropped item
        double velocity = 0.1;
        itemEntity.setVelocity(
            (wolf.getRandom().nextDouble() - 0.5) * velocity,
            0.2,
            (wolf.getRandom().nextDouble() - 0.5) * velocity
        );
        
        return wolf.getWorld().spawnEntity(itemEntity);
    }
    
    /**
     * Drops a specific item type from the inventory.
     * 
     * @param itemType The item type to drop
     * @param count The number of items to drop
     * @return The actual number of items dropped
     */
    public int dropItems(ItemType itemType, int count) {
        if (itemType == null || count <= 0) {
            return 0;
        }
        
        int totalDropped = 0;
        
        while (totalDropped < count) {
            int remaining = count - totalDropped;
            int inInventory = getItemCount(itemType);
            
            if (inInventory == 0) {
                break;
            }
            
            int toDrop = Math.min(remaining, Math.min(inInventory, itemType.getMaxStackSize()));
            int removed = removeItem(itemType, toDrop);
            
            if (removed > 0) {
                GameItem dropItem = new MinecraftItemWrapper(itemType, removed);
                if (dropItem(dropItem)) {
                    totalDropped += removed;
                } else {
                    // Failed to drop - add items back to inventory
                    addItem(dropItem);
                    break;
                }
            } else {
                break;
            }
        }
        
        return totalDropped;
    }
    
    /**
     * Drops all items of a specific type.
     * 
     * @param itemType The item type to drop
     * @return The number of items dropped
     */
    public int dropAllItems(ItemType itemType) {
        return dropItems(itemType, getItemCount(itemType));
    }
    
    /**
     * Drops all items from the inventory.
     * 
     * @return The number of item stacks dropped
     */
    public int dropAllItems() {
        int dropped = 0;
        List<GameItem> itemsToRestore = new ArrayList<>();
        
        for (GameItem item : new ArrayList<>(inventory)) {
            GameItem itemCopy = item.withCount(item.getCount());
            if (dropItem(itemCopy)) {
                dropped++;
            } else {
                itemsToRestore.add(item);
            }
        }
        
        clearInventory();
        inventory.addAll(itemsToRestore);
        
        return dropped;
    }
    
    /**
     * Converts a GameItem to a Minecraft ItemStack for infrastructure operations.
     * This provides the bridge between our clean abstractions and Minecraft's infrastructure.
     */
    private ItemStack convertToItemStack(GameItem item) {
        if (item instanceof MinecraftItemWrapper) {
            return ((MinecraftItemWrapper) item).getItemStack().copy();
        } else {
            // For non-Minecraft items (like fakes), create a new wrapper
            ItemType itemType = ItemType.fromMinecraftId(item.getItemType());
            return new MinecraftItemWrapper(itemType, item.getCount()).getItemStack();
        }
    }
}