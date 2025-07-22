package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.AiControlledWolfEntity;
import com.aimobs.entity.ai.EntityLookupService;
import com.aimobs.entity.ai.core.StatusIcon;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minecraft-specific implementation of status display for AI wolves.
 * Handles rendering status text and progress bars above entities.
 * 
 * Note: This is a simplified implementation. A full implementation would require
 * client-side rendering using Minecraft's rendering system and custom HUD elements.
 */
public class MinecraftStatusDisplayAdapter implements StatusDisplayAdapter {
    private final EntityLookupService entityLookupService;
    private final Map<UUID, StatusData> statusDisplays = new ConcurrentHashMap<>();
    
    public MinecraftStatusDisplayAdapter(EntityLookupService entityLookupService) {
        this.entityLookupService = entityLookupService;
    }
    
    @Override
    public void displayStatus(UUID wolfId, String status, StatusIcon icon) {
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf == null) {
            return;
        }
        
        StatusData statusData = new StatusData(status, icon, 0.0f);
        statusDisplays.put(wolfId, statusData);
        
        // In a full implementation, this would trigger client-side rendering
        // For now, we'll use the wolf's custom name as a simple status display
        updateWolfDisplayName(wolf, status, icon);
    }
    
    @Override
    public void displayProgress(UUID wolfId, float progress) {
        StatusData currentStatus = statusDisplays.get(wolfId);
        if (currentStatus != null) {
            StatusData updatedStatus = new StatusData(
                currentStatus.status(), 
                currentStatus.icon(), 
                progress
            );
            statusDisplays.put(wolfId, updatedStatus);
            
            AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
            if (wolf != null) {
                updateWolfDisplayName(wolf, currentStatus.status(), currentStatus.icon(), progress);
            }
        }
    }
    
    @Override
    public void clearDisplay(UUID wolfId) {
        statusDisplays.remove(wolfId);
        
        AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
        if (wolf != null) {
            wolf.setCustomName(null);
            wolf.setCustomNameVisible(false);
        }
    }
    
    @Override
    public void updateRendering() {
        // In a full implementation, this would update client-side rendering
        // For now, this method is a no-op as we're using simple name display
        
        // Remove any displays for wolves that no longer exist
        statusDisplays.entrySet().removeIf(entry -> {
            UUID wolfId = entry.getKey();
            AiControlledWolfEntity wolf = entityLookupService.findWolfById(wolfId);
            return wolf == null || wolf.isRemoved();
        });
    }
    
    /**
     * Updates the wolf's display name to show status information.
     * This is a simplified approach - a full implementation would use custom rendering.
     */
    private void updateWolfDisplayName(AiControlledWolfEntity wolf, String status, StatusIcon icon) {
        updateWolfDisplayName(wolf, status, icon, -1.0f);
    }
    
    /**
     * Updates the wolf's display name with status and optional progress.
     */
    private void updateWolfDisplayName(AiControlledWolfEntity wolf, String status, StatusIcon icon, float progress) {
        StringBuilder displayText = new StringBuilder();
        
        // Add icon representation
        if (icon != null) {
            displayText.append(getIconText(icon)).append(" ");
        }
        
        // Add status text
        displayText.append(status);
        
        // Add progress if available
        if (progress >= 0.0f) {
            int progressPercent = (int) (progress * 100);
            displayText.append(" (").append(progressPercent).append("%)");
        }
        
        wolf.setCustomName(Text.literal(displayText.toString()));
        wolf.setCustomNameVisible(true);
    }
    
    /**
     * Converts status icons to text representations.
     * In a full implementation, these would be actual icons/sprites.
     */
    private String getIconText(StatusIcon icon) {
        return switch (icon) {
            case MOVEMENT -> "➤";
            case COMBAT -> "⚔";
            case COLLECTION -> "📦";
            case DEFENSE -> "🛡";
            case IDLE -> "💤";
            case ERROR -> "❌";
            case PROCESSING -> "⏳";
        };
    }
    
    /**
     * Gets the current status data for a wolf.
     */
    public StatusData getStatusData(UUID wolfId) {
        return statusDisplays.get(wolfId);
    }
    
    /**
     * Record for storing status display data.
     */
    public record StatusData(String status, StatusIcon icon, float progress) {}
}