package com.aimobs.test;

import com.aimobs.entity.ai.core.GameItem;
import com.aimobs.entity.ai.core.InventoryActions;
import com.aimobs.entity.ai.core.ItemType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fake implementation of InventoryActions for testing.
 * Follows the fake object pattern used throughout the test suite.
 * Uses clean abstractions - no Minecraft dependencies.
 */
public class FakeInventoryActions implements InventoryActions {
    
    private final Map<ItemType, Integer> itemCounts = new HashMap<>();
    private final int maxCapacity;
    private int currentItemCount = 0;
    private final List<GameItem> droppedItems = new ArrayList<>();
    
    public FakeInventoryActions() {
        this(27); // Default capacity
    }
    
    public FakeInventoryActions(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
    
    @Override
    public boolean addItem(GameItem item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        
        if (!hasSpace(item)) {
            return false;
        }
        
        ItemType itemType = ItemType.fromMinecraftId(item.getItemType());
        int currentCount = itemCounts.getOrDefault(itemType, 0);
        itemCounts.put(itemType, currentCount + item.getCount());
        
        if (currentCount == 0) {
            currentItemCount++;
        }
        
        return true;
    }
    
    @Override
    public int removeItem(ItemType itemType, int count) {
        if (itemType == null || count <= 0) {
            return 0;
        }
        
        int currentCount = itemCounts.getOrDefault(itemType, 0);
        int toRemove = Math.min(count, currentCount);
        
        if (toRemove > 0) {
            int newCount = currentCount - toRemove;
            if (newCount == 0) {
                itemCounts.remove(itemType);
                currentItemCount--;
            } else {
                itemCounts.put(itemType, newCount);
            }
        }
        
        return toRemove;
    }
    
    @Override
    public int getItemCount(ItemType itemType) {
        return itemCounts.getOrDefault(itemType, 0);
    }
    
    @Override
    public List<GameItem> getAllItems() {
        List<GameItem> items = new ArrayList<>();
        for (Map.Entry<ItemType, Integer> entry : itemCounts.entrySet()) {
            items.add(FakeGameItem.of(entry.getKey(), entry.getValue()));
        }
        return items;
    }
    
    @Override
    public boolean hasSpace(GameItem item) {
        if (item == null || item.isEmpty()) {
            return true;
        }
        
        ItemType itemType = ItemType.fromMinecraftId(item.getItemType());
        if (itemCounts.containsKey(itemType)) {
            // Can stack with existing item
            return true;
        }
        
        // Need new slot
        return currentItemCount < maxCapacity;
    }
    
    @Override
    public int getItemCount() {
        return currentItemCount;
    }
    
    @Override
    public int getMaxCapacity() {
        return maxCapacity;
    }
    
    @Override
    public void clearInventory() {
        itemCounts.clear();
        currentItemCount = 0;
        droppedItems.clear();
    }
    
    @Override
    public boolean dropItem(GameItem item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        
        droppedItems.add(item.withCount(item.getCount()));
        return true;
    }
    
    // Test helper methods
    public void addTestItem(ItemType itemType, int count) {
        GameItem item = FakeGameItem.of(itemType, count);
        addItem(item);
    }
    
    public List<GameItem> getDroppedItems() {
        return new ArrayList<>(droppedItems);
    }
    
    public int getTotalDroppedItems() {
        return droppedItems.size();
    }
    
    public boolean containsItem(ItemType itemType) {
        return itemCounts.containsKey(itemType);
    }
    
    public void reset() {
        itemCounts.clear();
        currentItemCount = 0;
        droppedItems.clear();
    }
    
    // Helper to create common test items
    public static GameItem createTestItem(String itemType, int count) {
        ItemType type = switch (itemType.toLowerCase()) {
            case "wood" -> ItemType.OAK_LOG;
            case "stone" -> ItemType.STONE;
            case "apple" -> ItemType.APPLE;
            case "iron" -> ItemType.IRON_INGOT;
            case "diamond" -> ItemType.DIAMOND;
            default -> ItemType.STICK;
        };
        return FakeGameItem.of(type, count);
    }
}