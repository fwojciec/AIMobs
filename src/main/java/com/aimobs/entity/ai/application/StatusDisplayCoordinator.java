package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.StatusDisplayService;
import com.aimobs.entity.ai.core.StatusIcon;
import com.aimobs.entity.ai.infrastructure.StatusDisplayAdapter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Application service for coordinating status display above AI wolf entities.
 * Manages status display business logic including fade timing and status persistence.
 */
public class StatusDisplayCoordinator implements StatusDisplayService {
    private final StatusDisplayAdapter displayAdapter;
    private final Map<UUID, StatusInfo> statusMap = new ConcurrentHashMap<>();
    private final Map<UUID, Float> progressMap = new ConcurrentHashMap<>();
    private static final long STATUS_FADE_TIMEOUT_MS = 5000; // 5 seconds
    
    public StatusDisplayCoordinator(StatusDisplayAdapter displayAdapter) {
        this.displayAdapter = displayAdapter;
    }
    
    @Override
    public void updateStatus(UUID wolfId, String status) {
        updateStatus(wolfId, status, null);
    }
    
    @Override
    public void updateStatus(UUID wolfId, String status, StatusIcon icon) {
        StatusInfo statusInfo = new StatusInfo(status, icon, System.currentTimeMillis());
        statusMap.put(wolfId, statusInfo);
        displayAdapter.displayStatus(wolfId, status, icon);
    }
    
    @Override
    public void showProgress(UUID wolfId, float progress) {
        // Clamp progress between 0.0 and 1.0
        float clampedProgress = Math.max(0.0f, Math.min(1.0f, progress));
        progressMap.put(wolfId, clampedProgress);
        displayAdapter.displayProgress(wolfId, clampedProgress);
    }
    
    @Override
    public void clearStatus(UUID wolfId) {
        statusMap.remove(wolfId);
        progressMap.remove(wolfId);
        displayAdapter.clearDisplay(wolfId);
    }
    
    @Override
    public String getStatus(UUID wolfId) {
        StatusInfo info = statusMap.get(wolfId);
        return info != null ? info.status() : null;
    }
    
    @Override
    public float getProgress(UUID wolfId) {
        return progressMap.getOrDefault(wolfId, 0.0f);
    }
    
    @Override
    public StatusIcon getIcon(UUID wolfId) {
        StatusInfo info = statusMap.get(wolfId);
        return info != null ? info.icon() : null;
    }
    
    @Override
    public float getAlpha(UUID wolfId) {
        StatusInfo info = statusMap.get(wolfId);
        if (info == null) {
            return 0.0f;
        }
        
        long elapsed = System.currentTimeMillis() - info.timestamp();
        if (elapsed >= STATUS_FADE_TIMEOUT_MS) {
            return 0.0f;
        }
        
        // Calculate fade alpha (full opacity for first 3 seconds, then fade)
        long fadeStartTime = STATUS_FADE_TIMEOUT_MS - 2000; // Start fading 2 seconds before timeout
        if (elapsed < fadeStartTime) {
            return 1.0f;
        }
        
        float fadeProgress = (float) (elapsed - fadeStartTime) / 2000.0f;
        return 1.0f - fadeProgress;
    }
    
    /**
     * Should be called periodically to clean up expired status displays.
     */
    public void tick() {
        long currentTime = System.currentTimeMillis();
        statusMap.entrySet().removeIf(entry -> {
            long elapsed = currentTime - entry.getValue().timestamp();
            if (elapsed >= STATUS_FADE_TIMEOUT_MS) {
                displayAdapter.clearDisplay(entry.getKey());
                progressMap.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }
    
    /**
     * Internal record for tracking status information with timestamp.
     */
    private record StatusInfo(String status, StatusIcon icon, long timestamp) {}
}