package com.aimobs.entity.ai.infrastructure;

import com.aimobs.core.exceptions.ItemRegistryException;
import com.aimobs.entity.ai.core.GameItem;
import com.aimobs.entity.ai.core.ItemType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Infrastructure adapter that wraps Minecraft's ItemStack to implement GameItem.
 * Provides a seam between our clean core layer and Minecraft's infrastructure.
 * 
 * Following clean architecture principles:
 * - Infrastructure layer adapter
 * - Implements core interface using external framework classes
 * - Provides access to wrapped object for infrastructure operations
 */
public class MinecraftItemWrapper implements GameItem {
    
    private final ItemStack itemStack;
    private final ItemType itemType;
    
    public MinecraftItemWrapper(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.itemType = ItemType.fromMinecraftId(getMinecraftItemId(itemStack));
    }
    
    public MinecraftItemWrapper(ItemType itemType, int count) {
        this.itemType = itemType;
        this.itemStack = createItemStack(itemType, count);
    }
    
    @Override
    public String getItemType() {
        return itemType.getMinecraftId();
    }
    
    @Override
    public int getCount() {
        return itemStack.getCount();
    }
    
    @Override
    public void setCount(int count) {
        itemStack.setCount(count);
    }
    
    @Override
    public boolean isEmpty() {
        return itemStack.isEmpty();
    }
    
    @Override
    public boolean canCombineWith(GameItem other) {
        if (!(other instanceof MinecraftItemWrapper)) {
            // Can only combine with other Minecraft items
            return false;
        }
        
        MinecraftItemWrapper otherWrapper = (MinecraftItemWrapper) other;
        return ItemStack.canCombine(this.itemStack, otherWrapper.itemStack);
    }
    
    @Override
    public int getMaxStackSize() {
        return itemStack.getMaxCount();
    }
    
    @Override
    public GameItem withCount(int count) {
        ItemStack newStack = itemStack.copy();
        newStack.setCount(count);
        return new MinecraftItemWrapper(newStack);
    }
    
    @Override
    public String getDisplayName() {
        return itemStack.getName().getString();
    }
    
    /**
     * Gets the underlying Minecraft ItemStack for infrastructure operations.
     * This provides a seam for infrastructure components that need direct access
     * to Minecraft's ItemStack while maintaining clean architecture boundaries.
     * 
     * @return The underlying ItemStack implementation
     */
    public ItemStack getItemStack() {
        return itemStack;
    }
    
    /**
     * Gets the ItemType enum representation of this item.
     * 
     * @return The ItemType enum value
     */
    public ItemType getItemTypeEnum() {
        return itemType;
    }
    
    // Helper methods for creating ItemStacks from our abstractions
    private static ItemStack createItemStack(ItemType itemType, int count) {
        try {
            Identifier identifier = new Identifier(itemType.getMinecraftId());
            net.minecraft.item.Item minecraftItem = Registries.ITEM.get(identifier);
            
            if (minecraftItem != null) {
                return new ItemStack(minecraftItem, count);
            } else {
                // Fallback to air if item not found
                return ItemStack.EMPTY;
            }
        } catch (IllegalArgumentException e) {
            // Invalid item identifier format
            return ItemStack.EMPTY;
        } catch (NullPointerException e) {
            // Registry entry not found
            return ItemStack.EMPTY;
        } catch (RuntimeException e) {
            // Other registry access failures
            return ItemStack.EMPTY;
        }
    }
    
    private static String getMinecraftItemId(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return ItemType.AIR.getMinecraftId();
        }
        
        try {
            Identifier id = Registries.ITEM.getId(itemStack.getItem());
            return id != null ? id.toString() : ItemType.UNKNOWN.getMinecraftId();
        } catch (NullPointerException e) {
            // Registry ID not found
            return ItemType.UNKNOWN.getMinecraftId();
        } catch (RuntimeException e) {
            // Other registry access failures
            return ItemType.UNKNOWN.getMinecraftId();
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MinecraftItemWrapper)) return false;
        
        MinecraftItemWrapper other = (MinecraftItemWrapper) obj;
        return ItemStack.canCombine(this.itemStack, other.itemStack) &&
               this.itemStack.getCount() == other.itemStack.getCount();
    }
    
    @Override
    public int hashCode() {
        return itemStack.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("MinecraftItemWrapper{type=%s, count=%d}", 
                           getItemType(), getCount());
    }
}