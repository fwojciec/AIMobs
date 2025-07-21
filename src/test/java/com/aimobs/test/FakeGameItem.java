package com.aimobs.test;

import com.aimobs.entity.ai.core.GameItem;
import com.aimobs.entity.ai.core.ItemType;

/**
 * Fake implementation of GameItem for testing.
 * Follows the fake object pattern used throughout the test suite.
 * Eliminates need for Minecraft bootstrap in unit tests.
 */
public class FakeGameItem implements GameItem {
    
    private final ItemType itemType;
    private int count;
    
    public FakeGameItem(ItemType itemType, int count) {
        this.itemType = itemType != null ? itemType : ItemType.UNKNOWN;
        this.count = Math.max(0, count);
    }
    
    public FakeGameItem(ItemType itemType) {
        this(itemType, 1);
    }
    
    @Override
    public String getItemType() {
        return itemType.getMinecraftId();
    }
    
    @Override
    public int getCount() {
        return count;
    }
    
    @Override
    public void setCount(int count) {
        this.count = Math.max(0, count);
    }
    
    @Override
    public boolean isEmpty() {
        return count <= 0 || itemType == ItemType.AIR;
    }
    
    @Override
    public boolean canCombineWith(GameItem other) {
        if (other == null || other.isEmpty() || this.isEmpty()) {
            return false;
        }
        
        return this.getItemType().equals(other.getItemType());
    }
    
    @Override
    public int getMaxStackSize() {
        return itemType.getMaxStackSize();
    }
    
    @Override
    public GameItem withCount(int count) {
        return new FakeGameItem(itemType, count);
    }
    
    @Override
    public String getDisplayName() {
        return itemType.getDisplayName();
    }
    
    /**
     * Gets the ItemType enum for this fake item.
     * Useful for test assertions.
     * 
     * @return The ItemType enum value
     */
    public ItemType getItemTypeEnum() {
        return itemType;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof GameItem)) return false;
        
        GameItem other = (GameItem) obj;
        return this.getItemType().equals(other.getItemType()) &&
               this.getCount() == other.getCount();
    }
    
    @Override
    public int hashCode() {
        return getItemType().hashCode() * 31 + count;
    }
    
    @Override
    public String toString() {
        return String.format("FakeGameItem{type=%s, count=%d}", 
                           itemType.getDisplayName(), count);
    }
    
    // Test utility methods
    
    /**
     * Creates a fake item of the specified type with count 1.
     * 
     * @param itemType The item type
     * @return A new FakeGameItem instance
     */
    public static FakeGameItem of(ItemType itemType) {
        return new FakeGameItem(itemType, 1);
    }
    
    /**
     * Creates a fake item of the specified type and count.
     * 
     * @param itemType The item type
     * @param count The item count
     * @return A new FakeGameItem instance
     */
    public static FakeGameItem of(ItemType itemType, int count) {
        return new FakeGameItem(itemType, count);
    }
    
    /**
     * Creates an empty fake item.
     * 
     * @return A new empty FakeGameItem instance
     */
    public static FakeGameItem empty() {
        return new FakeGameItem(ItemType.AIR, 0);
    }
    
    /**
     * Creates a stack of stone items for testing.
     * 
     * @param count The number of stone items
     * @return A new FakeGameItem instance with stone
     */
    public static FakeGameItem stone(int count) {
        return new FakeGameItem(ItemType.STONE, count);
    }
    
    /**
     * Creates a stack of wood items for testing.
     * 
     * @param count The number of wood items
     * @return A new FakeGameItem instance with wood
     */
    public static FakeGameItem wood(int count) {
        return new FakeGameItem(ItemType.OAK_WOOD, count);
    }
    
    /**
     * Creates a single diamond for testing.
     * 
     * @return A new FakeGameItem instance with one diamond
     */
    public static FakeGameItem diamond() {
        return new FakeGameItem(ItemType.DIAMOND, 1);
    }
}