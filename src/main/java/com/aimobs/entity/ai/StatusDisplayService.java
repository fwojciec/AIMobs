package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.StatusIcon;
import java.util.UUID;

/**
 * Service interface for managing status display above AI wolf entities.
 * Provides real-time visual status information and progress indicators.
 */
public interface StatusDisplayService {
    /**
     * Updates the status text displayed above a wolf entity.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param status the status message to display
     */
    void updateStatus(UUID wolfId, String status);
    
    /**
     * Updates the status with an associated icon.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param status the status message to display
     * @param icon the icon to display alongside the status
     */
    void updateStatus(UUID wolfId, String status, StatusIcon icon);
    
    /**
     * Shows a progress indicator for long-running operations.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @param progress progress value between 0.0 and 1.0
     */
    void showProgress(UUID wolfId, float progress);
    
    /**
     * Clears the status display for a wolf entity.
     * 
     * @param wolfId the unique identifier of the wolf entity
     */
    void clearStatus(UUID wolfId);
    
    /**
     * Gets the current status for a wolf entity.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @return the current status message, or null if none
     */
    String getStatus(UUID wolfId);
    
    /**
     * Gets the current progress for a wolf entity.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @return the current progress value between 0.0 and 1.0
     */
    float getProgress(UUID wolfId);
    
    /**
     * Gets the current status icon for a wolf entity.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @return the current status icon, or null if none
     */
    StatusIcon getIcon(UUID wolfId);
    
    /**
     * Updates the display alpha for fade effects.
     * 
     * @param wolfId the unique identifier of the wolf entity
     * @return the current alpha value between 0.0 and 1.0
     */
    float getAlpha(UUID wolfId);
}