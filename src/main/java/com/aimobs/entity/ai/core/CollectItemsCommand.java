package com.aimobs.entity.ai.core;

/**
 * Command for collecting items within a specified area.
 * 
 * Part of the core layer - pure domain object with no dependencies.
 * Following Ben Johnson's standard package layout principles.
 */
public class CollectItemsCommand implements InteractionCommand {
    
    private final String itemType;
    private final double radius;
    private final int maxItems;
    private final int priority;
    private boolean isComplete = false;
    private boolean isCancelled = false;
    private int itemsCollected = 0;
    
    public CollectItemsCommand(String itemType, double radius, int maxItems) {
        this(itemType, radius, maxItems, InteractionType.COLLECT.getDefaultPriority());
    }
    
    public CollectItemsCommand(String itemType, double radius, int maxItems, int priority) {
        this.itemType = itemType;
        this.radius = radius;
        this.maxItems = maxItems;
        this.priority = priority;
    }
    
    @Override
    public void execute() {
        if (isCancelled) {
            isComplete = true;
            return;
        }
        // Execution logic will be handled by the goal system
    }
    
    @Override
    public boolean isComplete() {
        return isComplete || isCancelled || (maxItems > 0 && itemsCollected >= maxItems);
    }
    
    @Override
    public void cancel() {
        isCancelled = true;
        isComplete = true;
    }
    
    @Override
    public InteractionType getInteractionType() {
        return InteractionType.COLLECT;
    }
    
    @Override
    public boolean requiresPositioning() {
        return true;
    }
    
    @Override
    public int getPriority() {
        return priority;
    }
    
    /**
     * @return The type of items to collect
     */
    public String getItemType() {
        return itemType;
    }
    
    /**
     * @return The search radius for items
     */
    public double getRadius() {
        return radius;
    }
    
    /**
     * @return The maximum number of items to collect (0 for unlimited)
     */
    public int getMaxItems() {
        return maxItems;
    }
    
    /**
     * @return The number of items collected so far
     */
    public int getItemsCollected() {
        return itemsCollected;
    }
    
    /**
     * Increments the count of collected items.
     */
    public void incrementItemsCollected() {
        itemsCollected++;
    }
    
    /**
     * @return True if the command was cancelled
     */
    public boolean isCancelled() {
        return isCancelled;
    }
}