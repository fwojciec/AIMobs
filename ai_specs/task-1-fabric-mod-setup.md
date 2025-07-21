# Task 1: Basic Fabric Mod Setup

## Overview
Create a new Minecraft mod project from scratch for the AI-Powered Mob Controller system.

## Context
You need to set up a Fabric mod for Minecraft 1.20.4 that will serve as the foundation for AI-controlled Wolf entities. This mod will eventually receive commands via WebSocket and control Wolf behavior.

## Technical Requirements
- **Java Version**: Java 17 or newer
- **Minecraft Version**: 1.20.4 (latest stable)
- **Mod Loader**: Fabric
- **Dependencies**:
  - Fabric API 0.92.0
  - Fabric Loader 0.15.3
- **Build System**: Gradle 7.x
- **IDE**: IntelliJ IDEA with Minecraft Development plugin (recommended)
- **Testing Framework**: JUnit 5 + Fabric Test Mod
- **TDD Approach**: Red-Green-Refactor development cycle

## Entry Point
Starting with an empty directory, create a complete Fabric mod project structure that follows Fabric conventions and can be loaded in Minecraft 1.20.4.

## Requirements

### Project Structure
- Create proper Fabric mod directory structure
- Set up Gradle build configuration
- Configure mod metadata and entry points
- Implement basic mod initialization class

### Build Configuration
- `build.gradle` with correct Fabric dependencies
- Proper Java source/target version configuration
- Minecraft and Fabric version specifications
- Plugin configurations for Fabric development
- JUnit 5 and Fabric Test Mod dependencies
- Test task configuration for TDD workflow

### Mod Metadata
- `fabric.mod.json` with complete mod information:
  - Mod ID: `aimobs`
  - Mod name: "AI Mobs Controller"
  - Version: "1.0.0"
  - Description: "AI-powered voice-controlled Minecraft mob controller"
  - Authors, license, and contact information
  - Entry points for client and main initialization

### Basic Implementation
- Main mod class with proper initialization
- Client-side initialization if needed
- Basic logging setup
- Hello world functionality to verify loading

### TDD Setup
- Test source directories structure
- Base test classes and utilities
- Mock helpers for Minecraft objects
- Test configuration for Fabric environment

## Deliverables

### Required Files
1. **build.gradle**
   - Fabric plugin configuration
   - Dependencies for Fabric API and Loader
   - JUnit 5 and testing dependencies
   - Fabric Test Mod configuration
   - Java version configuration
   - Build and publish settings
   - Test task configuration

2. **gradle.properties**
   - Minecraft version
   - Fabric API version
   - Maven group and version

3. **src/main/resources/fabric.mod.json**
   - Complete mod metadata
   - Entry point definitions
   - Dependency specifications

4. **src/main/java/com/aimobs/AiMobsMod.java**
   - Main mod initialization class
   - Basic logging setup
   - OnInitialize implementation

5. **src/client/java/com/aimobs/AiMobsClient.java** (if needed)
   - Client-side initialization
   - Client-specific setup

### Test Files
6. **src/test/java/com/aimobs/ModInitializationTest.java**
   - Test mod loading and initialization
   - Verify mod metadata and entry points

7. **src/test/java/com/aimobs/TestHelper.java**
   - Common test utilities
   - Mock object creation helpers
   - Test environment setup

8. **src/testmod/java/com/aimobs/TestMod.java**
   - Fabric test mod entry point
   - Test environment configuration

### Directory Structure
```
AIMobs/
├── build.gradle
├── gradle.properties
├── settings.gradle
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── aimobs/
    │   │           └── AiMobsMod.java
    │   └── resources/
    │       ├── fabric.mod.json
    │       ├── aimobs.mixins.json
    │       └── assets/
    │           └── aimobs/
    │               └── icon.png
    ├── client/
    │   └── java/
    │       └── com/
    │           └── aimobs/
    │               └── AiMobsClient.java
    ├── test/
    │   ├── java/
    │   │   └── com/
    │   │       └── aimobs/
    │   │           ├── ModInitializationTest.java
    │   │           └── TestHelper.java
    │   └── resources/
    │       └── fabric.mod.json
    └── testmod/
        ├── java/
        │   └── com/
        │       └── aimobs/
        │           └── TestMod.java
        └── resources/
            └── fabric.mod.json
```

## TDD Implementation

### Red Phase - Write Failing Tests First

```java
// src/test/java/com/aimobs/ModInitializationTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class ModInitializationTest {
    
    @Test
    void shouldLoadModSuccessfully() {
        // Test that mod is loaded in Fabric environment
        assertTrue(FabricLoader.getInstance().isModLoaded("aimobs"));
    }
    
    @Test
    void shouldHaveCorrectModMetadata() {
        ModContainer mod = FabricLoader.getInstance()
            .getModContainer("aimobs").orElseThrow();
        
        assertEquals("AI Mobs Controller", mod.getMetadata().getName());
        assertEquals("1.0.0", mod.getMetadata().getVersion().getFriendlyString());
    }
    
    @Test
    void shouldInitializeWithoutErrors() {
        // Test that mod initialization doesn't throw exceptions
        assertDoesNotThrow(() -> new AiMobsMod().onInitialize());
    }
    
    @Test
    void shouldLogInitializationMessage() {
        // Test that mod logs successful initialization
        // This will fail initially until we implement logging
        // Implementation will use TestLogAppender to capture logs
    }
}
```

### Green Phase - Implement Minimal Code

```java
// src/main/java/com/aimobs/AiMobsMod.java
public class AiMobsMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("aimobs");
    
    @Override
    public void onInitialize() {
        LOGGER.info("AI Mobs Controller initialized successfully!");
    }
}
```

### Refactor Phase - Improve Implementation

## Validation Criteria

### Build Success
- [ ] `./gradlew build` completes without errors
- [ ] Generated JAR file exists in `build/libs/`
- [ ] No compilation warnings or errors

### Test Success
- [ ] `./gradlew test` passes all tests
- [ ] Tests run in Fabric environment
- [ ] Test coverage includes mod initialization

### Minecraft Integration
- [ ] Mod loads successfully in Minecraft 1.20.4 without errors
- [ ] Appears in Fabric mod list with correct name and version
- [ ] No console errors during Minecraft startup
- [ ] Basic "Hello World" log message appears in game log

### Development Environment
- [ ] IDE can import project without issues
- [ ] Hot reload/development environment works
- [ ] Debug logging functions properly

## Testing Instructions

### TDD Workflow
1. **Red Phase**:
   ```bash
   # Run tests (should fail initially)
   ./gradlew test
   ```

2. **Green Phase**:
   ```bash
   # Implement code to make tests pass
   ./gradlew test
   ```

3. **Refactor Phase**:
   ```bash
   # Improve code while keeping tests green
   ./gradlew test
   ```

### Integration Testing
4. **Build Test**:
   ```bash
   ./gradlew clean build
   ```

5. **Fabric Test Mod**:
   ```bash
   # Run tests in full Fabric environment
   ./gradlew runTestmod
   ```

6. **Development Test**:
   ```bash
   # Run in development environment
   ./gradlew runClient
   ```

## Success Criteria
The task is complete when:
- All deliverable files exist and are properly configured
- All TDD tests pass (`./gradlew test`)
- Mod builds successfully without errors
- Mod loads in Minecraft 1.20.4 and appears in mod list
- Fabric Test Mod environment works
- Basic logging functionality works
- Development environment is fully functional
- Test coverage includes core mod functionality

## Dependencies
- None (this is the foundation task)

## Integration Notes
This mod will later integrate with:
- WebSocket client for external communication
- Custom Fox entity implementation
- Command processing system
- Visual feedback systems

## Build Configuration Example

### build.gradle
```gradle
plugins {
    id 'fabric-loom' version '1.4-SNAPSHOT'
    id 'maven-publish'
}

dependencies {
    minecraft "com.mojang:minecraft:1.20.4"
    mappings "net.fabricmc:yarn:1.20.4+build.3:v2"
    modImplementation "net.fabricmc:fabric-loader:0.15.3"
    modImplementation "net.fabricmc.fabric-api:fabric-api:0.92.0+1.20.4"

    // Testing dependencies
    testImplementation 'org.junit.jupiter:junit-jupiter:5.9.2'
    testImplementation 'org.mockito:mockito-core:5.1.1'
    testImplementation 'org.assertj:assertj-core:3.24.2'
    testImplementation 'net.fabricmc:fabric-loader-junit:0.15.3'
}

test {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
    }
}

loom {
    runs {
        testmod {
            inherit server
            name "Test Mod"
            source sourceSets.testmod
        }
    }
}

sourceSets {
    testmod {
        compileClasspath += main.compileClasspath
        runtimeClasspath += main.runtimeClasspath
    }
}
```

## Troubleshooting
Common issues and solutions:
- **Build failures**: Check Java version and Gradle wrapper
- **Test failures**: Verify Fabric Test Mod configuration
- **Mod not loading**: Verify fabric.mod.json syntax and entry points
- **Version conflicts**: Ensure Minecraft/Fabric versions match exactly
- **Import issues**: Check IDE Minecraft development plugin installation
- **TDD setup**: Ensure JUnit 5 and Fabric test dependencies are correct