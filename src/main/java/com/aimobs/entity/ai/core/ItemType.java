package com.aimobs.entity.ai.core;

/**
 * Enumeration of common item types used in the game.
 * Provides clean abstraction over Minecraft item identifiers.
 * 
 * Following clean architecture principles:
 * - Core layer enum with no external dependencies
 * - Maps to Minecraft items via infrastructure adapters
 */
public enum ItemType {
    
    // Common blocks
    STONE("minecraft:stone", "Stone", 64),
    COBBLESTONE("minecraft:cobblestone", "Cobblestone", 64),
    OAK_WOOD("minecraft:oak_wood", "Oak Wood", 64),
    OAK_LOG("minecraft:oak_log", "Oak Log", 64),
    DIRT("minecraft:dirt", "Dirt", 64),
    GRASS_BLOCK("minecraft:grass_block", "Grass Block", 64),
    
    // Common items
    STICK("minecraft:stick", "Stick", 64),
    COAL("minecraft:coal", "Coal", 64),
    IRON_INGOT("minecraft:iron_ingot", "Iron Ingot", 64),
    GOLD_INGOT("minecraft:gold_ingot", "Gold Ingot", 64),
    DIAMOND("minecraft:diamond", "Diamond", 64),
    
    // Food items
    WHEAT("minecraft:wheat", "Wheat", 64),
    BREAD("minecraft:bread", "Bread", 64),
    APPLE("minecraft:apple", "Apple", 64),
    
    // Tools (lower stack sizes)
    WOODEN_SWORD("minecraft:wooden_sword", "Wooden Sword", 1),
    STONE_SWORD("minecraft:stone_sword", "Stone Sword", 1),
    IRON_SWORD("minecraft:iron_sword", "Iron Sword", 1),
    
    // Special types
    AIR("minecraft:air", "Air", 64),
    UNKNOWN("unknown", "Unknown Item", 1);
    
    private final String minecraftId;
    private final String displayName;
    private final int maxStackSize;
    
    ItemType(String minecraftId, String displayName, int maxStackSize) {
        this.minecraftId = minecraftId;
        this.displayName = displayName;
        this.maxStackSize = maxStackSize;
    }
    
    /**
     * Gets the Minecraft identifier for this item type.
     * Used by infrastructure adapters to map to actual Minecraft items.
     * 
     * @return The Minecraft item identifier
     */
    public String getMinecraftId() {
        return minecraftId;
    }
    
    /**
     * Gets the human-readable display name for this item.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the maximum stack size for this item type.
     * 
     * @return The maximum number of items that can be in one stack
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }
    
    /**
     * Finds an ItemType by its Minecraft identifier.
     * 
     * @param minecraftId The Minecraft item identifier
     * @return The corresponding ItemType, or UNKNOWN if not found
     */
    public static ItemType fromMinecraftId(String minecraftId) {
        if (minecraftId == null) {
            return UNKNOWN;
        }
        
        for (ItemType type : values()) {
            if (type.minecraftId.equals(minecraftId)) {
                return type;
            }
        }
        
        return UNKNOWN;
    }
    
    /**
     * Finds an ItemType by its display name (case-insensitive).
     * 
     * @param displayName The display name to search for
     * @return The corresponding ItemType, or UNKNOWN if not found
     */
    public static ItemType fromDisplayName(String displayName) {
        if (displayName == null) {
            return UNKNOWN;
        }
        
        String lowerName = displayName.toLowerCase().trim();
        for (ItemType type : values()) {
            if (type.displayName.toLowerCase().equals(lowerName)) {
                return type;
            }
        }
        
        return UNKNOWN;
    }
}