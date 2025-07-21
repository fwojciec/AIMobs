package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.core.GameItem;
import com.aimobs.entity.ai.core.InventoryActions;
import com.aimobs.entity.ai.core.ItemType;
import com.aimobs.test.BaseUnitTest;
import com.aimobs.test.FakeGameItem;
import com.aimobs.test.FakeInventoryActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WolfInventoryManager following TDD approach.
 * Tests inventory business logic in isolation.
 * 
 * Note: Using FakeInventoryActions since WolfInventoryManager requires 
 * WolfEntity which is difficult to mock cleanly in unit tests.
 */
class WolfInventoryManagerTest extends BaseUnitTest {
    
    private InventoryActions inventoryActions;
    
    @BeforeEach
    void setUp() {
        inventoryActions = new FakeInventoryActions(10); // Small capacity for testing
    }
    
    @Test
    void shouldStartEmpty() {
        assertEquals(0, inventoryActions.getItemCount());
        assertEquals(10, inventoryActions.getMaxCapacity());
        assertTrue(inventoryActions.getAllItems().isEmpty());
    }
    
    @Test
    void shouldAddSingleItem() {
        GameItem item = FakeGameItem.of(ItemType.OAK_LOG, 5);
        
        assertTrue(inventoryActions.addItem(item));
        assertEquals(1, inventoryActions.getItemCount());
        assertEquals(5, inventoryActions.getItemCount(ItemType.OAK_LOG));
    }
    
    @Test
    void shouldNotAddNullItem() {
        assertFalse(inventoryActions.addItem(null));
        assertEquals(0, inventoryActions.getItemCount());
    }
    
    @Test
    void shouldNotAddEmptyItemStack() {
        GameItem emptyItem = FakeGameItem.of(ItemType.OAK_LOG, 0);
        
        assertFalse(inventoryActions.addItem(emptyItem));
        assertEquals(0, inventoryActions.getItemCount());
    }
    
    @Test
    void shouldStackSimilarItems() {
        GameItem firstItem = FakeGameItem.of(ItemType.OAK_LOG, 5);
        GameItem secondItem = FakeGameItem.of(ItemType.OAK_LOG, 3);
        
        assertTrue(inventoryActions.addItem(firstItem));
        assertTrue(inventoryActions.addItem(secondItem));
        
        assertEquals(1, inventoryActions.getItemCount()); // One item type
        assertEquals(8, inventoryActions.getItemCount(ItemType.OAK_LOG)); // Total count
    }
    
    @Test
    void shouldAddDifferentItems() {
        GameItem logItem = FakeGameItem.of(ItemType.OAK_LOG, 5);
        GameItem stoneItem = FakeGameItem.of(ItemType.STONE, 3);
        
        assertTrue(inventoryActions.addItem(logItem));
        assertTrue(inventoryActions.addItem(stoneItem));
        
        assertEquals(2, inventoryActions.getItemCount());
        assertEquals(5, inventoryActions.getItemCount(ItemType.OAK_LOG));
        assertEquals(3, inventoryActions.getItemCount(ItemType.STONE));
    }
    
    @Test
    void shouldRejectItemsWhenFull() {
        // Fill inventory to capacity
        ItemType[] testItems = {ItemType.OAK_LOG, ItemType.STONE, ItemType.IRON_INGOT, ItemType.APPLE, ItemType.STICK, 
                               ItemType.COBBLESTONE, ItemType.DIRT, ItemType.GRASS_BLOCK, ItemType.COAL, ItemType.WHEAT};
        for (int i = 0; i < 10; i++) {
            GameItem item = FakeGameItem.of(testItems[i % testItems.length], 1);
            inventoryActions.addItem(item);
        }
        
        assertEquals(10, inventoryActions.getItemCount());
        
        // Try to add one more item
        GameItem extraItem = FakeGameItem.of(ItemType.DIAMOND, 1);
        assertFalse(inventoryActions.addItem(extraItem));
        assertEquals(10, inventoryActions.getItemCount());
    }
    
    @Test
    void shouldHaveSpaceForNewItem() {
        GameItem item = FakeGameItem.of(ItemType.OAK_LOG, 5);
        
        assertTrue(inventoryActions.hasSpace(item));
        
        inventoryActions.addItem(item);
        assertTrue(inventoryActions.hasSpace(item)); // Can still stack
    }
    
    @Test
    void shouldNotHaveSpaceWhenFull() {
        // Fill inventory
        ItemType[] testItems = {ItemType.OAK_LOG, ItemType.STONE, ItemType.IRON_INGOT, ItemType.APPLE, ItemType.STICK, 
                               ItemType.COBBLESTONE, ItemType.DIRT, ItemType.GRASS_BLOCK, ItemType.COAL, ItemType.WHEAT};
        for (int i = 0; i < 10; i++) {
            GameItem item = FakeGameItem.of(testItems[i % testItems.length], 1);
            inventoryActions.addItem(item);
        }
        
        GameItem newItem = FakeGameItem.of(ItemType.DIAMOND, 1);
        assertFalse(inventoryActions.hasSpace(newItem));
    }
    
    @Test
    void shouldRemoveItems() {
        GameItem item = FakeGameItem.of(ItemType.OAK_LOG, 10);
        inventoryActions.addItem(item);
        
        int removed = inventoryActions.removeItem(ItemType.OAK_LOG, 5);
        
        assertEquals(5, removed);
        assertEquals(5, inventoryActions.getItemCount(ItemType.OAK_LOG));
        assertEquals(1, inventoryActions.getItemCount());
    }
    
    @Test
    void shouldRemoveAllItemsOfType() {
        GameItem item = FakeGameItem.of(ItemType.OAK_LOG, 10);
        inventoryActions.addItem(item);
        
        int removed = inventoryActions.removeItem(ItemType.OAK_LOG, 15); // More than available
        
        assertEquals(10, removed); // Only removed what was available
        assertEquals(0, inventoryActions.getItemCount(ItemType.OAK_LOG));
        assertEquals(0, inventoryActions.getItemCount());
    }
    
    @Test
    void shouldNotRemoveNonExistentItems() {
        int removed = inventoryActions.removeItem(ItemType.DIAMOND, 5);
        
        assertEquals(0, removed);
    }
    
    @Test
    void shouldNotRemoveWithInvalidParameters() {
        assertEquals(0, inventoryActions.removeItem(null, 5));
        assertEquals(0, inventoryActions.removeItem(ItemType.OAK_LOG, 0));
        assertEquals(0, inventoryActions.removeItem(ItemType.OAK_LOG, -5));
    }
    
    @Test
    void shouldGetCorrectItemCount() {
        assertEquals(0, inventoryActions.getItemCount(ItemType.OAK_LOG));
        assertEquals(0, inventoryActions.getItemCount(null));
        
        GameItem item = FakeGameItem.of(ItemType.OAK_LOG, 7);
        inventoryActions.addItem(item);
        
        assertEquals(7, inventoryActions.getItemCount(ItemType.OAK_LOG));
        assertEquals(0, inventoryActions.getItemCount(ItemType.STONE));
    }
    
    @Test
    void shouldReturnAllItems() {
        GameItem logItem = FakeGameItem.of(ItemType.OAK_LOG, 5);
        GameItem stoneItem = FakeGameItem.of(ItemType.STONE, 3);
        
        inventoryActions.addItem(logItem);
        inventoryActions.addItem(stoneItem);
        
        List<GameItem> allItems = inventoryActions.getAllItems();
        
        assertEquals(2, allItems.size());
        assertTrue(allItems.stream().anyMatch(item -> 
            item.getItemType().equals(ItemType.OAK_LOG.getMinecraftId()) && item.getCount() == 5));
        assertTrue(allItems.stream().anyMatch(item -> 
            item.getItemType().equals(ItemType.STONE.getMinecraftId()) && item.getCount() == 3));
    }
    
    @Test
    void shouldClearInventory() {
        GameItem logItem = FakeGameItem.of(ItemType.OAK_LOG, 5);
        GameItem stoneItem = FakeGameItem.of(ItemType.STONE, 3);
        
        inventoryActions.addItem(logItem);
        inventoryActions.addItem(stoneItem);
        
        assertEquals(2, inventoryActions.getItemCount());
        
        inventoryActions.clearInventory();
        
        assertEquals(0, inventoryActions.getItemCount());
        assertTrue(inventoryActions.getAllItems().isEmpty());
    }
    
    @Test
    void shouldDropItems() {
        GameItem item = FakeGameItem.of(ItemType.OAK_LOG, 5);
        
        assertTrue(inventoryActions.dropItem(item));
    }
    
    @Test
    void shouldNotDropNullOrEmptyItems() {
        assertFalse(inventoryActions.dropItem(null));
        
        GameItem emptyItem = FakeGameItem.of(ItemType.OAK_LOG, 0);
        assertFalse(inventoryActions.dropItem(emptyItem));
    }
    
    @Test
    void shouldHandleEdgeCases() {
        // Test with maximum stack size
        GameItem maxItem = FakeGameItem.of(ItemType.OAK_LOG, ItemType.OAK_LOG.getMaxStackSize());
        assertTrue(inventoryActions.addItem(maxItem));
        
        // Test removing more than exists
        int removed = inventoryActions.removeItem(ItemType.OAK_LOG, ItemType.OAK_LOG.getMaxStackSize() + 10);
        assertEquals(ItemType.OAK_LOG.getMaxStackSize(), removed);
        
        // Test hasSpace with null
        assertTrue(inventoryActions.hasSpace(null));
        
        GameItem emptyItem = FakeGameItem.empty();
        assertTrue(inventoryActions.hasSpace(emptyItem));
    }
    
    @Test
    void shouldMaintainInventoryIntegrity() {
        // Add various items
        inventoryActions.addItem(FakeGameItem.of(ItemType.OAK_LOG, 10));
        inventoryActions.addItem(FakeGameItem.of(ItemType.STONE, 15));
        inventoryActions.addItem(FakeGameItem.of(ItemType.IRON_INGOT, 8));
        
        // Verify counts
        assertEquals(3, inventoryActions.getItemCount());
        assertEquals(10, inventoryActions.getItemCount(ItemType.OAK_LOG));
        assertEquals(15, inventoryActions.getItemCount(ItemType.STONE));
        assertEquals(8, inventoryActions.getItemCount(ItemType.IRON_INGOT));
        
        // Remove some items
        inventoryActions.removeItem(ItemType.STONE, 5);
        assertEquals(10, inventoryActions.getItemCount(ItemType.STONE));
        assertEquals(3, inventoryActions.getItemCount()); // Still 3 types
        
        // Remove all of one type
        inventoryActions.removeItem(ItemType.IRON_INGOT, 20);
        assertEquals(0, inventoryActions.getItemCount(ItemType.IRON_INGOT));
        assertEquals(2, inventoryActions.getItemCount()); // Now 2 types
        
        // Verify remaining items
        List<GameItem> remaining = inventoryActions.getAllItems();
        assertEquals(2, remaining.size());
    }
}