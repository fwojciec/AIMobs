package com.aimobs.entity.ai.core;

import java.util.List;

/**
 * Core interface defining inventory actions for an entity.
 * Part of the core layer - no dependencies on other layers.
 * 
 * Following Ben Johnson's standard package layout:
 * - Core layer contains only interfaces and value objects
 * - No dependencies on infrastructure or application layers
 */
public interface InventoryActions {
    
    /**
     * Adds an item to the entity's inventory.
     * 
     * @param item The item to add
     * @return True if the item was successfully added
     */
    boolean addItem(GameItem item);
    
    /**
     * Removes an item from the entity's inventory.
     * 
     * @param itemType The type of item to remove
     * @param count The number of items to remove
     * @return The actual number of items removed
     */
    int removeItem(ItemType itemType, int count);
    
    /**
     * Gets the count of a specific item type in the inventory.
     * 
     * @param itemType The item type to count
     * @return The number of items of this type
     */
    int getItemCount(ItemType itemType);
    
    /**
     * Gets all items currently in the inventory.
     * 
     * @return A list of all items in the inventory
     */
    List<GameItem> getAllItems();
    
    /**
     * Checks if the inventory has space for the specified item.
     * 
     * @param item The item to check
     * @return True if there is space
     */
    boolean hasSpace(GameItem item);
    
    /**
     * Gets the current number of items in the inventory.
     * 
     * @return The total number of item stacks
     */
    int getItemCount();
    
    /**
     * Gets the maximum capacity of the inventory.
     * 
     * @return The maximum number of item stacks that can be stored
     */
    int getMaxCapacity();
    
    /**
     * Clears all items from the inventory.
     */
    void clearInventory();
    
    /**
     * Drops an item at the entity's current location.
     * 
     * @param item The item to drop
     * @return True if the item was successfully dropped
     */
    boolean dropItem(GameItem item);
}