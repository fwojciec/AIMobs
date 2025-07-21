# Test Setup Documentation

## Overview

This document explains the comprehensive testing setup for the AIMobs Fabric mod, designed to support the full feature scope including WebSocket integration, movement commands, and entity behavior.

**Key Learning:** Test pain reveals design problems. When tests are hard to write, the design has dependency issues that need architectural fixes, not test workarounds.

## Testing Strategy

We use a **layered testing approach** that provides comprehensive coverage while maintaining fast development cycles:

### 1. Unit Tests (Fast & Isolated)
- ✅ Fast execution (< 1 second)
- ✅ No external dependencies  
- ✅ Test business logic and algorithms
- ✅ Command processing, state management
- ✅ JSON parsing and validation

### 2. Integration Tests (Fabric Environment)
- ✅ WebSocket client/server integration
- ✅ Command queue processing
- ✅ Network error handling
- ✅ Mock server communication
- ✅ Threading and async behavior

### 3. Game Tests (Future - Full Minecraft Environment)
- 🔄 Entity spawning and behavior
- 🔄 In-world movement and pathfinding
- 🔄 Player interaction testing
- 🔄 Visual feedback validation

### Test Categories

## Current Test Configuration

### Dependencies

```gradle
// Testing dependencies
testImplementation 'org.junit.jupiter:junit-jupiter:5.10.1'
testImplementation 'org.mockito:mockito-core:5.8.0'
testImplementation 'org.assertj:assertj-core:3.24.2'
testImplementation 'org.awaitility:awaitility:4.2.0'

// WebSocket testing dependencies
testImplementation 'org.java-websocket:Java-WebSocket:1.5.3'
testImplementation 'com.google.code.gson:gson:2.10.1'
```

### Java 24 Compatibility

**Important:** If using Java 24, enable experimental ByteBuddy support for Mockito:

```gradle
test {
    // Fix Mockito for Java 24
    jvmArgs '-Dnet.bytebuddy.experimental=true'
}
```

### Test Structure

Tests inherit from main compilation classpath:

```gradle
sourceSets {
    test {
        compileClasspath += main.compileClasspath
        runtimeClasspath += main.runtimeClasspath
    }
}
```

### Test Configuration

```gradle
test {
    useJUnitPlatform()
    testLogging {
        events "passed", "skipped", "failed"
        exceptionFormat "full"
    }
}
```

## Running Tests

### Command Line
```bash
./gradlew test
```

### With Verbose Output
```bash
./gradlew test --info
```

### Prerequisites
- Java 17+ (configured in PATH)
- Gradle wrapper (./gradlew)

## Test Examples

### Basic Unit Test
```java
@Test
void shouldPassBasicTest() {
    assertTrue(true, "Basic test should pass");
}
```

### Mod Class Validation
```java
@Test
void shouldHaveModMainClass() {
    assertDoesNotThrow(() -> {
        Class.forName("com.aimobs.AiMobsMod");
    }, "Mod main class should be loadable");
}
```

## Testability Principles

### The Seam Pattern (Michael Feathers)

**A seam is a place where you can alter behavior without editing the code there.**

```java
// ❌ Hard to test - concrete dependencies
public class BadWebSocketManager {
    private WebSocketClient client; // Concrete class - no seam
    
    public void connect(String url) {
        client = new WebSocketClient(URI.create(url)) { ... }; // Hard dependency
    }
}

// ✅ Easy to test - interface seam
public class GoodWebSocketService {
    private final WebSocketConnection connection; // Interface - seam!
    
    public GoodWebSocketService(WebSocketConnection connection) {
        this.connection = connection; // Injected dependency
    }
}
```

### Testing Strategy by Component Type

#### 1. Pure Business Logic - Use Real Objects
```java
@Test
void shouldValidateNetworkMessage() {
    // Real domain objects - no mocking needed
    NetworkMessage message = new NetworkMessage("command", "2025-01-19T10:00:00Z", data);
    assertTrue(message.isValid());
}
```

#### 2. Service Coordination - Use Fakes When Possible
```java
@Test
void shouldHandleWebSocketFlow() {
    // Fake objects - predictable, no external dependencies
    FakeWebSocketConnection connection = new FakeWebSocketConnection();
    FakeMessageService messageService = new FakeMessageService();
    
    WebSocketService service = new TestableWebSocketService(connection, coordinator);
    // Test complete flows deterministically
}
```

#### 3. External Dependencies - Use Mocks Sparingly
```java
@Test
void shouldCallExternalService() {
    // Mock only when you can't control the external dependency
    ExternalService mockService = mock(ExternalService.class);
    // Test the interaction, not the implementation
}
```

### When Tests Are Hard to Write

**Test pain indicates design problems:**

1. **"I need to mock 5 dependencies"** → Class doing too much (SRP violation)
2. **"I can't test without starting Minecraft"** → Missing abstraction layer  
3. **"Tests are flaky and slow"** → Hidden dependencies on external systems
4. **"I can't swap implementations"** → Depending on concrete classes, not interfaces

**Solution:** Create seams (interfaces) and use dependency injection.

## Adding New Tests

### For New Features

1. **Start with interfaces** - define the contract first
2. **Create test doubles** - fakes for complex dependencies, mocks for simple ones
3. **Test through interfaces** - never test concrete classes directly
4. **Follow naming convention** - `*Test.java` for units, `*IntegrationTest.java` for integration

### Test Structure Template

```java
@Tag("unit") // or "integration"
class NewFeatureTest extends BaseUnitTest {
    
    private NewFeatureService service;
    private FakeDependency fakeDependency;
    
    @BeforeEach
    void setUp() {
        fakeDependency = new FakeDependency();
        service = ServiceFactory.createNewFeatureService(fakeDependency);
    }
    
    @Test
    void shouldHandleHappyPath() {
        // Given
        // When  
        // Then
    }
}
```

## Future Enhancements

If full integration testing becomes necessary:

1. **Game Tests**: Use Fabric's game test framework
2. **Client Tests**: Set up Minecraft client environment
3. **Server Tests**: Test server-side functionality

### Example Integration Test Setup (Future)
```gradle
// For future integration testing
testImplementation "net.fabricmc:fabric-loader-junit:${project.loader_version}"

test {
    systemProperty "fabric.development", "true"
}
```

## Troubleshooting

### Java PATH Issues
If tests fail with "Java Runtime not found":
```bash
source ~/.zprofile
./gradlew test
```

### Compilation Errors
Ensure test sourceSet inherits from main:
- Check `sourceSets` configuration in `build.gradle`
- Verify dependencies are properly declared

### Test Framework Issues
- Use JUnit 5 (`org.junit.jupiter`)
- Avoid mixing JUnit 4 and 5
- Use `@Test` not `@org.junit.Test`

## Validation Workflow

For any code changes:

1. Run tests: `./gradlew test`
2. Check all tests pass (100% success rate)
3. Review test output for any warnings
4. Add new tests for new functionality

## Performance

Current test suite:
- **3 tests** in **~0.2 seconds**
- Scales linearly with additional unit tests
- No external network calls or file I/O