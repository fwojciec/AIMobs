package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.core.StatusIcon;
import java.util.UUID;

/**
 * Infrastructure adapter interface for status display above AI wolf entities.
 * Handles the platform-specific implementation of status visualization.
 */
public interface StatusDisplayAdapter {
    /**
     * Displays status text above a wolf entity.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param status the status message to display
     * @param icon the icon to display alongside the status (can be null)
     */
    void displayStatus(UUID wolfId, String status, StatusIcon icon);
    
    /**
     * Displays a progress bar for a wolf entity.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param progress progress value between 0.0 and 1.0
     */
    void displayProgress(UUID wolfId, float progress);
    
    /**
     * Clears the status display for a wolf entity.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void clearDisplay(UUID wolfId);
    
    /**
     * Updates the rendering for all active displays.
     * Should be called each client tick.
     */
    void updateRendering();
}