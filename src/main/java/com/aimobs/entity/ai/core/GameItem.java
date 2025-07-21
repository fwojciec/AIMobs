package com.aimobs.entity.ai.core;

/**
 * Core abstraction for game items.
 * Provides a clean interface for item operations without Minecraft dependencies.
 * 
 * Following clean architecture principles:
 * - Core layer interface with no external dependencies
 * - Implemented by infrastructure adapters and test fakes
 */
public interface GameItem {
    
    /**
     * Gets the type identifier for this item.
     * 
     * @return The item type (e.g., "minecraft:stone", "minecraft:wood")
     */
    String getItemType();
    
    /**
     * Gets the quantity of this item.
     * 
     * @return The item count
     */
    int getCount();
    
    /**
     * Sets the quantity of this item.
     * 
     * @param count The new item count
     */
    void setCount(int count);
    
    /**
     * Checks if this item stack is empty.
     * 
     * @return True if count is 0 or item is null/invalid
     */
    boolean isEmpty();
    
    /**
     * Checks if this item can be combined with another item.
     * Items can be combined if they are the same type.
     * 
     * @param other The other item to check
     * @return True if items can be stacked together
     */
    boolean canCombineWith(GameItem other);
    
    /**
     * Gets the maximum stack size for this item type.
     * 
     * @return The maximum number of items that can be in one stack
     */
    int getMaxStackSize();
    
    /**
     * Creates a copy of this item with the specified count.
     * 
     * @param count The count for the new item
     * @return A new GameItem instance with the specified count
     */
    GameItem withCount(int count);
    
    /**
     * Gets a display name for this item suitable for user interfaces.
     * 
     * @return A human-readable name for this item
     */
    String getDisplayName();
}