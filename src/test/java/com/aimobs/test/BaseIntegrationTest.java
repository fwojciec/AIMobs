package com.aimobs.test;

import org.junit.jupiter.api.Tag;

/**
 * Base class for integration tests that may involve networking, file I/O, etc.
 * These tests can be slower and may involve external dependencies.
 * 
 * For full Minecraft integration, use the testmod source set instead.
 */
@Tag("integration")
public abstract class BaseIntegrationTest {
    
    /**
     * Setup method for integration tests.
     * Override in subclasses for specific test setup.
     */
    protected void setUp() {
        // Common setup for integration tests
    }
    
    /**
     * Cleanup method for integration tests.
     * Override in subclasses for specific cleanup.
     */
    protected void tearDown() {
        // Common cleanup for integration tests
    }
}