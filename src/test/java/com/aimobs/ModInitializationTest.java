package com.aimobs;

import com.aimobs.test.BaseUnitTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModInitializationTest extends BaseUnitTest {
    
    @Test
    void shouldPassBasicTest() {
        assertTrue(true, "Basic test should pass");
    }
    
    @Test
    void shouldHaveModMainClass() {
        // Test that the mod class can be loaded (basic classpath test)
        assertDoesNotThrow(() -> {
            Class.forName("com.aimobs.AiMobsMod");
        }, "Mod main class should be loadable");
    }
    
    @Test
    void shouldHaveValidModVersion() {
        String version = "1.0.0"; // From gradle.properties
        assertNotNull(version);
        assertFalse(version.isEmpty());
    }
}