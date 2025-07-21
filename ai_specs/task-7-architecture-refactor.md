# Task 7: Architecture Compliance Refactor

## Overview

Refactor the persistence system to fully comply with `@ai_docs/architecture_principles.md` by eliminating architecture violations and creating a clean, testable design that follows interface-first development and proper layer separation.

## Current Architecture Violations

### Critical Issues
1. **MessageParser** (Application) imports and uses Minecraft classes
2. **EntityResolver** (Application) imports and uses Minecraft classes  
3. **Mixed entity lookup responsibilities** across layers
4. **Direct platform dependencies** in business logic layer

### Specific Violations
```java
// ❌ WRONG: Application layer importing Infrastructure
// In MessageParser.java (Application)
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

// ❌ WRONG: Application layer with platform-specific logic
private AiControlledWolfEntity findWolfByEntityId(EntityId entityId) {
    for (Entity entity : serverWorld.iterateEntities()) { // Platform code in Application!
        // ...
    }
}
```

## Target Architecture

### Clean Layer Separation
```
com.aimobs.entity.ai/
├── [Root Interfaces]              ← Service contracts (no dependencies)
│   ├── CommandReceiver            ← NEW: Pure command recipient contract
│   ├── EntityLookupService        ← NEW: Pure entity lookup contract
│   ├── CommandRoutingService      ← NEW: Pure command routing contract
│   └── EntityLifecycleService     ← EXISTING: Already clean
├── core/                          ← Domain primitives (no dependencies)  
│   ├── EntityId                   ← EXISTING: Already clean
│   └── AiEntityState              ← EXISTING: Already clean
├── application/                   ← Business logic (Root + Core only)
│   ├── MessageParser              ← REFACTOR: Remove Minecraft dependencies
│   ├── CommandRouter              ← NEW: Pure command routing logic
│   └── EntityLifecycleCoordinator ← EXISTING: Already clean
└── infrastructure/                ← Platform integration (all layers)
    ├── MinecraftEntityLookup      ← NEW: Minecraft-specific entity finding
    ├── MinecraftCommandReceiver   ← NEW: Minecraft wolf adapter
    └── MinecraftWorldEventHandler ← EXISTING: Already clean
```

## Implementation Plan

### Phase 1: Create Clean Root Interfaces

#### 1.1 CommandReceiver Interface
**File:** `src/main/java/com/aimobs/entity/ai/CommandReceiver.java`

```java
package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AICommand;

/**
 * Service contract for entities that can receive and process AI commands.
 * Abstracts command delivery from entity implementation details.
 * 
 * Root interface - defines what the system does (contract only).
 * Dependencies: Core layer only.
 */
public interface CommandReceiver {
    
    /**
     * Gets the unique identifier for this command receiver.
     * Used for routing commands to specific entities.
     * 
     * @return The unique entity identifier
     */
    EntityId getEntityId();
    
    /**
     * Receives and processes an AI command.
     * Implementation handles command queuing and execution.
     * 
     * @param command The command to process
     */
    void receiveCommand(AICommand command);
    
    /**
     * Checks if this receiver is available to process commands.
     * Considers entity state, health, and readiness.
     * 
     * @return true if the receiver can accept commands
     */
    boolean isAvailable();
    
    /**
     * Gets the current command queue size for this receiver.
     * Used for load balancing and monitoring.
     * 
     * @return Number of queued commands
     */
    int getQueuedCommandCount();
}
```

#### 1.2 EntityLookupService Interface
**File:** `src/main/java/com/aimobs/entity/ai/EntityLookupService.java`

```java
package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.EntityId;

import java.util.List;
import java.util.Optional;

/**
 * Service contract for finding and resolving command receivers.
 * Abstracts entity lookup from platform-specific implementation.
 * 
 * Root interface - defines what the system does (contract only).
 * Dependencies: Core layer only.
 */
public interface EntityLookupService {
    
    /**
     * Finds a specific command receiver by its unique identifier.
     * Used for routing commands to specific entities after world reload.
     * 
     * @param entityId The unique identifier of the entity
     * @return Optional containing the command receiver, or empty if not found
     */
    Optional<CommandReceiver> findEntityById(EntityId entityId);
    
    /**
     * Gets all available command receivers in the current context.
     * Returns entities that are ready to process commands.
     * 
     * @return List of available command receivers
     */
    List<CommandReceiver> getAllAvailableEntities();
    
    /**
     * Finds any available command receiver for command processing.
     * Used when commands don't target a specific entity.
     * 
     * @return Optional containing an available receiver, or empty if none found
     */
    Optional<CommandReceiver> findAnyAvailable();
    
    /**
     * Gets the count of available command receivers.
     * Used for monitoring and load balancing decisions.
     * 
     * @return Number of available receivers
     */
    int getAvailableEntityCount();
}
```

#### 1.3 CommandRoutingService Interface
**File:** `src/main/java/com/aimobs/entity/ai/CommandRoutingService.java`

```java
package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AICommand;

import java.util.Optional;

/**
 * Service contract for routing commands to appropriate command receivers.
 * Handles command delivery logic and routing decisions.
 * 
 * Root interface - defines what the system does (contract only).
 * Dependencies: Core layer only.
 */
public interface CommandRoutingService {
    
    /**
     * Routes a command to an appropriate receiver.
     * If targetId is provided, routes to specific entity.
     * Otherwise, routes to any available entity.
     * 
     * @param command The command to route
     * @param targetId Optional target entity ID for specific routing
     * @return true if command was successfully routed
     */
    boolean routeCommand(AICommand command, Optional<EntityId> targetId);
    
    /**
     * Routes a command to any available receiver.
     * Uses load balancing to distribute commands fairly.
     * 
     * @param command The command to route
     * @return true if command was successfully routed
     */
    boolean routeToAnyAvailable(AICommand command);
    
    /**
     * Gets the number of available command receivers.
     * Used for routing decisions and monitoring.
     * 
     * @return Number of available receivers
     */
    int getAvailableReceiverCount();
    
    /**
     * Checks if any receivers are available for command processing.
     * 
     * @return true if at least one receiver is available
     */
    boolean hasAvailableReceivers();
}
```

### Phase 2: Refactor Application Layer

#### 2.1 Clean MessageParser
**File:** `src/main/java/com/aimobs/entity/ai/application/MessageParser.java`

**Changes:**
- Remove all Minecraft imports
- Remove entity lookup logic
- Use CommandRoutingService interface
- Pure message processing only

```java
public class MessageParser implements MessageService {
    private final Gson gson;
    private final Queue<AICommand> commandQueue;
    private final CommandProcessorService commandProcessor;
    private final CommandRoutingService commandRouter; // NEW: Interface only
    
    // NO Minecraft imports or entity lookup logic
    // Pure business logic for message parsing and command creation
}
```

#### 2.2 Create CommandRouter
**File:** `src/main/java/com/aimobs/entity/ai/application/CommandRouter.java`

```java
package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.CommandRoutingService;
import com.aimobs.entity.ai.EntityLookupService;
import com.aimobs.entity.ai.CommandReceiver;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.ai.core.AICommand;

/**
 * Application service implementing command routing logic.
 * Pure business logic for distributing commands to entities.
 * 
 * Application layer - implements service contracts.
 * Dependencies: Root interfaces + Core only.
 */
public class CommandRouter implements CommandRoutingService {
    
    private final EntityLookupService entityLookup;
    
    public CommandRouter(EntityLookupService entityLookup) {
        this.entityLookup = entityLookup;
    }
    
    // Pure business logic - no platform dependencies
    // Command routing, load balancing, retry logic
}
```

### Phase 3: Infrastructure Layer

#### 3.1 MinecraftEntityLookupService
**File:** `src/main/java/com/aimobs/entity/ai/infrastructure/MinecraftEntityLookupService.java`

```java
package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.EntityLookupService;
import com.aimobs.entity.ai.CommandReceiver;
import com.aimobs.entity.ai.core.EntityId;
import com.aimobs.entity.AiControlledWolfEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

/**
 * Infrastructure implementation of EntityLookupService.
 * Handles Minecraft-specific entity finding and lookup.
 * 
 * Infrastructure layer - can depend on all other layers.
 * Contains platform-specific implementation details.
 */
public class MinecraftEntityLookupService implements EntityLookupService {
    
    private final ServerWorld world;
    
    public MinecraftEntityLookupService(ServerWorld world) {
        this.world = world;
    }
    
    // All Minecraft-specific entity iteration and lookup logic
}
```

#### 3.2 Update AiControlledWolfEntity
**File:** `src/main/java/com/aimobs/entity/AiControlledWolfEntity.java`

**Changes:**
- Implement CommandReceiver interface
- Clean up entity lookup responsibilities

```java
public class AiControlledWolfEntity extends WolfEntity 
        implements CommandExecutor, EntityActions, CommandReceiver {
    
    // Implement CommandReceiver methods
    @Override
    public EntityId getEntityId() { return entityId; }
    
    @Override
    public void receiveCommand(AICommand command) {
        getCommandQueue().offer(command);
    }
    
    @Override
    public boolean isAvailable() {
        return isAlive() && !getCommandQueue().isEmpty();
    }
    
    @Override
    public int getQueuedCommandCount() {
        return getCommandQueue().size();
    }
}
```

### Phase 4: Update Composition Root

#### 4.1 ServiceFactory Updates
**File:** `src/main/java/com/aimobs/entity/ai/ServiceFactory.java`

**New Methods:**
```java
/**
 * Create entity lookup service with world context.
 */
public static EntityLookupService createEntityLookupService(ServerWorld world) {
    return new MinecraftEntityLookupService(world);
}

/**
 * Create command routing service with entity lookup.
 */
public static CommandRoutingService createCommandRoutingService(EntityLookupService entityLookup) {
    return new CommandRouter(entityLookup);
}

/**
 * Create message service with command routing.
 */
public static MessageService createMessageService(
        CommandProcessorService commandProcessor,
        CommandRoutingService commandRouter) {
    return new MessageParser(commandProcessor, commandRouter);
}
```

### Phase 5: Update World Event Handler

#### 5.1 MinecraftWorldEventHandler
**File:** `src/main/java/com/aimobs/entity/ai/infrastructure/MinecraftWorldEventHandler.java`

**Updates:**
```java
private void onWorldLoad(MinecraftServer server, ServerWorld world) {
    // Create persistence services
    AiPersistenceService persistenceService = ServiceFactory.createAiPersistenceService(world);
    EntityLifecycleService lifecycleService = ServiceFactory.createEntityLifecycleService(persistenceService);
    
    // Create entity lookup and command routing services
    EntityLookupService entityLookup = ServiceFactory.createEntityLookupService(world);
    CommandRoutingService commandRouter = ServiceFactory.createCommandRoutingService(entityLookup);
    
    // Reconnect AI entities
    lifecycleService.reconnectAiEntities();
    
    // Configure MessageParser with clean interfaces
    MessageService messageService = AiMobsMod.getMessageService();
    if (messageService instanceof MessageParser parser) {
        parser.setCommandRouter(commandRouter); // Interface only!
    }
}
```

## Testing Strategy

### Unit Tests (Application Layer)
```java
// Test with fake objects - no Minecraft
public class CommandRouterTest {
    private CommandRouter router;
    private FakeEntityLookupService entityLookup;
    
    @BeforeEach
    void setUp() {
        entityLookup = new FakeEntityLookupService();
        router = new CommandRouter(entityLookup);
    }
    
    @Test
    void shouldRouteCommandToSpecificEntity() {
        // Pure business logic testing
    }
}
```

### Integration Tests (Infrastructure Layer)
```java
// Test with mocked Minecraft components
public class MinecraftEntityLookupServiceTest {
    private MinecraftEntityLookupService service;
    private ServerWorld mockWorld;
    
    // Test Minecraft integration with mocks
}
```

### End-to-End Tests
```java
// Test complete command flow
public class CommandFlowIntegrationTest {
    // Test message → parse → route → execute flow
}
```

## Migration Plan

### Step-by-Step Execution

#### Step 1: Create Interfaces (30 minutes)
1. Create CommandReceiver interface
2. Create EntityLookupService interface  
3. Create CommandRoutingService interface
4. Compile and verify no errors

#### Step 2: Infrastructure Layer (45 minutes)
1. Create MinecraftEntityLookupService
2. Update AiControlledWolfEntity to implement CommandReceiver
3. Add ServiceFactory methods
4. Test infrastructure components

#### Step 3: Application Layer (60 minutes)
1. Create CommandRouter implementation
2. Refactor MessageParser to remove Minecraft dependencies
3. Update method signatures
4. Test business logic

#### Step 4: Integration (45 minutes)
1. Update MinecraftWorldEventHandler
2. Update mod initialization
3. Update SpawnAiWolfCommand
4. Test complete integration

#### Step 5: Testing (60 minutes)
1. Add unit tests for new components
2. Update existing tests
3. Add integration tests
4. Verify all scenarios work

### Rollback Plan
If issues arise, each step can be reverted independently:
1. Keep original files as `.backup`
2. Commit after each successful step
3. Have working build at each checkpoint

## Validation Checklist

### Architecture Compliance
- [ ] No Application layer imports of Infrastructure
- [ ] All business logic uses interfaces only
- [ ] Clean dependency flow (Infrastructure → Application → Core)
- [ ] Composition root wires all dependencies

### Functionality Preserved
- [ ] Wolf entities spawn correctly
- [ ] Commands route to entities
- [ ] Persistence works across save/reload
- [ ] All existing tests pass

### Testability Achieved
- [ ] Application layer testable with fakes
- [ ] Business logic isolated from platform
- [ ] Fast unit tests (< 100ms)
- [ ] Infrastructure tested with mocks

## Benefits Achieved

### Architectural Benefits
- ✅ Perfect compliance with architecture principles
- ✅ Clean separation of concerns
- ✅ Interface-first development
- ✅ Testable business logic

### Future Benefits
- ✅ Easy to add new entity types
- ✅ Easy to swap implementations
- ✅ Clear extension points
- ✅ Maintainable codebase

### Development Benefits
- ✅ Fast feedback loops
- ✅ Confident refactoring
- ✅ Parallel development possible
- ✅ Clear debugging boundaries

## Estimated Effort

**Total Time:** 4-5 hours
**Risk Level:** Medium (well-defined interfaces reduce risk)
**Value:** High (clean architecture for future development)

## Conclusion

This refactor eliminates all architecture violations while preserving functionality. The investment in clean architecture will pay dividends in development speed, testability, and maintainability for all future features.

The plan follows interface-first development principles and ensures that business logic is completely separated from platform concerns, creating a foundation for robust, extensible AI entity management.