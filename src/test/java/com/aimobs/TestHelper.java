package com.aimobs;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.junit.jupiter.api.Assumptions;

public class TestHelper {
    
    public static void assumeModLoaded(String modId) {
        Assumptions.assumeTrue(FabricLoader.getInstance().isModLoaded(modId),
            "Mod " + modId + " is not loaded");
    }
    
    public static ModContainer getModContainer(String modId) {
        return FabricLoader.getInstance()
            .getModContainer(modId)
            .orElseThrow(() -> new IllegalStateException("Mod " + modId + " not found"));
    }
    
    public static void verifyModMetadata(String modId, String expectedName, String expectedVersion) {
        ModContainer mod = getModContainer(modId);
        String actualName = mod.getMetadata().getName();
        String actualVersion = mod.getMetadata().getVersion().getFriendlyString();
        
        if (!expectedName.equals(actualName)) {
            throw new AssertionError("Expected mod name: " + expectedName + ", but was: " + actualName);
        }
        
        if (!expectedVersion.equals(actualVersion)) {
            throw new AssertionError("Expected mod version: " + expectedVersion + ", but was: " + actualVersion);
        }
    }
}