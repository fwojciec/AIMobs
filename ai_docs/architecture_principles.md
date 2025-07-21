# Architecture Principles

## Overview

This document outlines the architectural principles and patterns used in the AIMobs project. These principles ensure maintainability, testability, and extensibility as the project grows to include WebSocket integration, AI command processing, and complex entity behaviors.

## Core Architectural Philosophy

We follow **layered architecture** principles with **clear separation of concerns** and **dependency inversion** to create a system that is:

- **Testable** - Every component can be tested in isolation via interfaces
- **Extensible** - New features fit naturally without architectural changes  
- **Maintainable** - Clear separation of concerns and dependency flow
- **Modular** - Well-defined boundaries between different system concerns

The core principle is **interface-first development** where we define contracts (interfaces) before implementations, enabling easy testing and flexibility.

### Critical Design Rule: Interface-First Development

**Always create interfaces before implementations.** This prevents the common mistake of hard dependencies that make testing difficult.

```java
// ❌ WRONG: Concrete class first, hard to test
public class WebSocketManager {
    private JavaWebSocketClient client; // Concrete dependency
}

// ✅ CORRECT: Interface first, easy to test  
public interface WebSocketConnection { /* contract */ }
public class WebSocketManager {
    private final WebSocketConnection connection; // Interface dependency
}
```

## Package Layout Structure

### Package Layout Principles

Our package structure enforces unidirectional dependencies and clear layer boundaries:

```
com.aimobs.entity.ai/              (Root Package)
├── [Interfaces]                   ← Service contracts
├── ServiceFactory.java            ← Composition root
├── core/                          ← Value objects & core contracts
├── application/                   ← Business logic implementations
└── infrastructure/                ← Platform-specific implementations
```

### Dependency Flow Rules

**✅ ALLOWED Dependencies:**
```
Infrastructure → Application → Core
Infrastructure → Root Interfaces
Application → Root Interfaces
Root → Core (only)
```

**❌ FORBIDDEN Dependencies:**
```
Application → Infrastructure  (peer packages)
Core → Application           (dependency inversion)
Core → Infrastructure        (dependency inversion)
Root → Application           (wrong direction)
Root → Infrastructure        (wrong direction)
```

## Layer Definitions

### 1. Root Package - Service Contracts
**Purpose:** Define what the system does (contracts)
**Contents:** Interfaces only
**Dependencies:** Core layer only
**Examples:** `CommandProcessorService`, `GoalService`

```java
// Example: Root interface
public interface CommandProcessorService {
    void executeCommand(AICommand command);
    AIState getCurrentState();
}
```

### 2. Core Package - Domain Primitives
**Purpose:** Fundamental types and contracts
**Contents:** Interfaces, enums, value objects
**Dependencies:** None (pure domain)
**Examples:** `AICommand`, `AIState`, `EntityActions`

```java
// Example: Core interface
public interface AICommand {
    void execute();
    boolean isComplete();
    void cancel();
}
```

### 3. Application Package - Business Logic
**Purpose:** How the system implements its contracts
**Contents:** Service implementations, use cases
**Dependencies:** Root interfaces + Core only
**Examples:** `CommandProcessor`, `GoalCoordinator`

```java
// Example: Application service
public class CommandProcessor implements CommandProcessorService {
    // Pure business logic - no infrastructure concerns
}
```

### 4. Infrastructure Package - Platform Integration
**Purpose:** Where the system runs (platform-specific)
**Contents:** Minecraft adapters, external integrations
**Dependencies:** All other layers
**Examples:** `MinecraftControllableGoal`, `WebSocketAdapter`

```java
// Example: Infrastructure adapter
public class MinecraftControllableGoal extends Goal {
    // Minecraft-specific implementation
}
```

## Composition Root Pattern

### ServiceFactory - Dependency Injection
All object construction and wiring happens in a single place:

```java
public class ServiceFactory {
    // Return interfaces, construct concrete implementations
    public static CommandProcessorService createCommandProcessor(Queue<AICommand> queue) {
        return new CommandProcessor(queue);
    }
    
    public static GoalService createGoalService(EntityActions actions) {
        return new GoalCoordinator(actions);
    }
}
```

### Benefits
- **Single source of truth** for object construction
- **Easy testing** - swap implementations for tests
- **Clear dependencies** - all wiring visible in one place
- **Flexible deployment** - different implementations for different environments

## Testing Architecture

### Test Layer Separation
Our tests follow the same architectural principles:

```java
// Unit tests use service interfaces
public class CommandProcessorTest {
    private CommandProcessorService processor;
    
    @BeforeEach
    void setUp() {
        processor = ServiceFactory.createCommandProcessor(mockQueue);
    }
}
```

### Test Categories
1. **Unit Tests** - Test business logic using interfaces
2. **Integration Tests** - Test service coordination  
3. **Infrastructure Tests** - Test Minecraft integration

### Mocking Strategy
- **Mock external dependencies** (Minecraft APIs)
- **Use real objects for domain logic** (AICommand, AIState)
- **Test through interfaces** (CommandProcessorService, not CommandProcessor)

## SOLID Principles Application

### Single Responsibility Principle (SRP)
- **CommandProcessor** - handles command execution only
- **GoalCoordinator** - handles goal setup only
- **ServiceFactory** - handles object construction only

### Open/Closed Principle (OCP)
- **Open for extension** - new implementations via interfaces
- **Closed for modification** - existing code unchanged when adding features

### Liskov Substitution Principle (LSP)
- Any implementation of `CommandProcessorService` can replace another
- Interfaces define behavioral contracts

### Interface Segregation Principle (ISP)
- **Small, focused interfaces** - `AICommand` has 3 methods
- **Role-based interfaces** - `EntityActions` for entity operations

### Dependency Inversion Principle (DIP)
- **Depend on abstractions** - use interfaces, not concrete classes
- **High-level modules don't depend on low-level modules**

## Design Patterns Used

### 1. Service Layer Pattern
**Purpose:** Encapsulate business logic in services
**Implementation:** `CommandProcessor`, `GoalCoordinator`
**Benefits:** Testable business logic, clear API boundaries

### 2. Adapter Pattern  
**Purpose:** Adapt external APIs to internal interfaces
**Implementation:** `MinecraftEntityActions`, `MinecraftControllableGoal`
**Benefits:** Isolate platform-specific code

### 3. Factory Pattern
**Purpose:** Centralize object construction
**Implementation:** `ServiceFactory`
**Benefits:** Dependency injection, flexible instantiation

### 4. Strategy Pattern
**Purpose:** Swap algorithms/implementations
**Implementation:** `AICommand` implementations
**Benefits:** Extensible command system

### 5. Composition Root Pattern
**Purpose:** Single place for dependency wiring
**Implementation:** `ServiceFactory` + entity constructors
**Benefits:** Clear dependency graph, testability

### 6. Seam Pattern (Michael Feathers)
**Purpose:** Enable behavior substitution without code modification
**Implementation:** Interface boundaries that allow test double injection
**Benefits:** Testability, runtime behavior modification

```java
// The seam is the WebSocketConnection interface
public class TestableWebSocketService implements WebSocketService {
    private final WebSocketConnection connection; // ← SEAM
    
    // Production: inject JavaWebSocketConnection
    // Testing: inject FakeWebSocketConnection
    public TestableWebSocketService(WebSocketConnection connection) {
        this.connection = connection;
    }
}
```

## Testability Architecture

### The Interface Contract Principle

Every service must have an interface defining its contract:

```java
// 1. Define the contract
public interface MessageService {
    NetworkMessage parseMessage(String rawMessage);
    boolean validateMessage(NetworkMessage message);
    AICommand convertToCommand(NetworkMessage message);
}

// 2. Implement the business logic
public class MessageParser implements MessageService {
    // Pure business logic - no external dependencies
}

// 3. Use through interface only
public class WebSocketManager {
    private final MessageService messageService; // Interface, not concrete class
}
```

### Seam Categories for Testing

#### 1. Pure Business Logic Seams
**What:** Value objects, domain logic, calculations
**Testing:** Use real objects, no mocking needed
**Example:** `NetworkMessage`, validation logic

```java
@Test
void shouldValidateMessage() {
    NetworkMessage message = new NetworkMessage("command", timestamp, data);
    assertTrue(message.isValid()); // Real object, real logic
}
```

#### 2. Service Coordination Seams  
**What:** Orchestration between services, workflows
**Testing:** Use fake objects for predictable behavior
**Example:** `WebSocketService` coordinating connection and message parsing

```java
@Test  
void shouldHandleMessageFlow() {
    FakeWebSocketConnection connection = new FakeWebSocketConnection();
    FakeMessageService messageService = new FakeMessageService();
    
    WebSocketService service = new TestableWebSocketService(connection, coordinator);
    // Test complete workflows deterministically
}
```

#### 3. External Integration Seams
**What:** Platform APIs, external libraries, I/O operations
**Testing:** Use mocks only when you can't control the dependency
**Example:** Minecraft entity APIs, file system, network calls

```java
@Test
void shouldCallMinecraftAPI() {
    Entity mockEntity = mock(Entity.class); // External API we can't control
    EntityActions actions = new MinecraftEntityActions(mockEntity);
    // Test interaction patterns, not implementation
}
```

### Anti-Patterns That Hurt Testability

#### ❌ Concrete Dependencies
```java
public class BadService {
    private final ExternalLibrary library = new ExternalLibrary(); // Hard dependency
    
    public void doWork() {
        library.call(); // Can't substitute for testing
    }
}
```

#### ❌ Static Dependencies
```java
public class BadService {
    public void doWork() {
        StaticUtility.doSomething(); // Hidden dependency, can't substitute
    }
}
```

#### ❌ Mixed Responsibilities  
```java
public class BadService {
    public void processMessage(String raw) {
        // Parse JSON (business logic)
        // Validate message (business logic)  
        // Send to WebSocket (infrastructure concern)
        // Update database (infrastructure concern)
        // Too many responsibilities!
    }
}
```

#### ✅ Seam-Based Design
```java
public class GoodService implements MessageService {
    private final NetworkConnection connection;    // Seam
    private final DatabaseService database;        // Seam
    
    public GoodService(NetworkConnection connection, DatabaseService database) {
        this.connection = connection;   // Injected dependencies
        this.database = database;
    }
    
    public void processMessage(String raw) {
        NetworkMessage message = parseMessage(raw);        // Pure logic
        if (validateMessage(message)) {                    // Pure logic
            connection.send(message);                      // Through seam
            database.save(message);                        // Through seam
        }
    }
}
```

## Future Architecture Extensions

### WebSocket Integration (Task 3)
Will fit naturally in our architecture:

```
com.aimobs.network/
├── WebSocketService.java         ← Root interface
├── MessageService.java           ← Root interface  
├── application/
│   ├── WebSocketManager.java     ← Implementation
│   └── MessageParser.java        ← Implementation
└── infrastructure/
    └── JavaWebSocketAdapter.java ← Platform adapter
```

### Movement Commands (Task 4)
Will extend existing patterns:

```java
// New command implementations
public class MoveCommand implements AICommand { }
public class FollowCommand implements AICommand { }

// Service extensions
public interface MovementService {
    void moveTo(Position target);
    void follow(Entity entity);
}
```

### AI Behaviors (Task 5+)
Will use the same service pattern:

```java
public interface AIBehaviorService {
    void processDecision(AIDecision decision);
    AIStrategy selectStrategy(GameState state);
}
```

## Code Quality Standards

### Naming Conventions
- **Interfaces:** Descriptive names ending in "Service" for root interfaces
- **Implementations:** Domain names (CommandProcessor, not CommandProcessorImpl)
- **Factories:** ServiceFactory, ComponentFactory
- **Tests:** [ClassName]Test with descriptive method names

### Documentation Standards
- **Package-level documentation** explaining layer purpose
- **Interface documentation** defining contracts
- **Implementation documentation** explaining business logic
- **Test documentation** describing behavior being verified

### Dependency Guidelines
- **Never import peer packages** (application ↔ infrastructure)
- **Always depend on interfaces** when possible
- **Keep core package dependency-free**
- **Use composition root for all wiring**

## Validation and Compliance

### Architecture Validation
To ensure compliance with these principles:

1. **Dependency Analysis** - No forbidden imports between layers
2. **Interface Coverage** - Business logic accessed through interfaces
3. **Test Coverage** - All services testable in isolation
4. **Documentation Review** - Architecture decisions documented

### Checklist for New Features

#### Architecture Compliance
- [ ] Does it fit in an existing layer?
- [ ] Are dependencies unidirectional?
- [ ] Does it follow SOLID principles?
- [ ] Is the composition root updated?

#### Testability Requirements (Critical!)
- [ ] **Interface created BEFORE implementation?**
- [ ] **Can be tested without external dependencies?**
- [ ] **Seams identified for substitution points?**
- [ ] **No concrete class dependencies in constructor?**
- [ ] **Can write fast (< 100ms) unit tests?**

#### Design Quality Gates
- [ ] Business logic separated from infrastructure concerns?
- [ ] Single responsibility per class?
- [ ] Dependency injection used for all collaborators?
- [ ] Test doubles (fakes/mocks) can be easily created?

#### Red Flags (Fix These!)
- [ ] ❌ "I need to start Minecraft to test this"
- [ ] ❌ "I need to mock 5+ dependencies" 
- [ ] ❌ "Tests are slow or flaky"
- [ ] ❌ "I can't test this in isolation"

If any red flags are present, **stop coding and fix the design first.**

## Benefits Achieved

### For Development
- **Fast tests** - No Minecraft bootstrap required
- **Easy debugging** - Clear separation of concerns  
- **Confident refactoring** - Interfaces protect against breaking changes
- **Parallel development** - Teams can work on different layers

### For Maintenance  
- **Clear boundaries** - Easy to locate code for specific concerns
- **Minimal coupling** - Changes isolated to specific layers
- **Easy onboarding** - Architecture principles guide new developers
- **Future-proof** - New features fit existing patterns

### For Testing
- **Unit testable** - Every component tested in isolation
- **Integration testable** - Layer interactions verified
- **Mockable dependencies** - External systems easily stubbed
- **Fast feedback** - Tests run in < 1 second

## References and Further Reading

- **Ben Johnson's Standard Package Layout:** [go package layout](https://medium.com/@benbjohnson/standard-package-layout-7cdbc8391fc1)
- **Ports and Adapters Architecture (Hexagonal Architecture)**
- **Working Effectively with Legacy Code by Michael Feathers**
- **Test-Driven Development by Kent Beck**
- **SOLID Principles by Robert C. Martin**

## Conclusion

This architecture provides a solid foundation for building complex AI-driven Minecraft mods. By following these principles, we ensure that the codebase remains maintainable, testable, and extensible as we implement WebSocket integration, movement commands, and advanced AI behaviors.

The investment in architectural discipline pays dividends in development speed, code quality, and system reliability. Every new feature should reinforce these patterns, creating a virtuous cycle of architectural improvement.