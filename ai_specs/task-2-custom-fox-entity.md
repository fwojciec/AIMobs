# Task 2: Custom Wolf Entity Implementation

## Overview
Create a custom Wolf entity class that overrides the default Minecraft Wolf AI to enable programmatic control.

## Context
The default Wolf entity has autonomous AI that needs to be replaced with a controllable version. You need to create a custom entity that maintains Wolf appearance/behavior but allows external command control while disabling default autonomous behaviors.

## Technical Requirements
- **Base Class**: Extend `WolfEntity` or implement custom entity extending `TameableEntity`
- **AI Override**: Replace default AI goals with controllable system
- **Model/Texture**: Maintain original Wolf appearance and animations
- **Registration**: Proper entity type registration with Fabric
- **Spawning**: Custom spawn methods for testing and deployment
- **Testing Framework**: JUnit 5 + Fabric Test Mod for entity testing
- **TDD Approach**: Test-driven development for all entity functionality

## Entry Point
Starting with the basic Fabric mod from Task 1, implement a custom Wolf entity that can be spawned and controlled programmatically, replacing the default Wolf AI behavior with a command-driven system.

## Requirements

### Entity Class Implementation
- Create `AiControlledWolfEntity` class extending appropriate base
- Override AI goal registration to prevent default behaviors
- Implement custom AI goal management system
- Maintain Wolf model, textures, and sound effects
- Preserve Wolf-specific properties (variants, collar colors, taming state, etc.)

### AI System Override
- Remove default AI goals:
  - `RandomLookAroundGoal`
  - `WanderAroundFarGoal` 
  - `FollowParentGoal`
  - `AnimalMateGoal`
  - `TemptGoal`
  - Default following owner/sitting behaviors
- Implement controllable AI goal system
- Add idle state management
- Maintain essential goals (swimming, panic from damage)

### Entity Registration
- Register custom entity type with Fabric
- Configure spawn settings and properties
- Set up entity attributes (health, speed, etc.)
- Register entity data trackers if needed

### Spawning System
- Implement spawn egg for testing
- Create command for spawning controlled Fox
- Add method to convert existing Fox to controlled version
- Ensure proper entity initialization

## Deliverables

### Required Files

1. **src/main/java/com/aimobs/entity/AiControlledWolfEntity.java**
   - Custom Wolf entity class
   - AI goal management system
   - Command execution interface
   - State management (idle, executing, etc.)

2. **src/main/java/com/aimobs/entity/ModEntities.java**
   - Entity type registration
   - Entity attributes registration
   - Spawn egg registration

3. **src/main/java/com/aimobs/entity/ai/ControllableGoal.java**
   - Base class for controllable AI goals
   - Command queue interface
   - Execution state management

4. **src/main/java/com/aimobs/command/SpawnAiWolfCommand.java**
   - Command for spawning controlled Wolf
   - Admin/testing command implementation

### Test Files

5. **src/test/java/com/aimobs/entity/AiControlledWolfEntityTest.java**
   - TDD tests for Wolf entity functionality
   - AI behavior testing
   - Command interface testing

6. **src/test/java/com/aimobs/entity/ModEntitiesTest.java**
   - Entity registration testing
   - Spawn testing

7. **src/testmod/java/com/aimobs/testmod/EntityTestMod.java**
   - Test mod for in-game entity testing

### Resource Files

8. **src/main/resources/data/aimobs/loot_tables/entities/ai_controlled_wolf.json**
   - Loot table for custom Wolf entity

9. **src/main/resources/assets/aimobs/lang/en_us.json**
   - Language file for entity name and spawn egg

### Integration Updates

10. **Update fabric.mod.json**
    - Add entity registration entry point

11. **Update AiMobsMod.java**
    - Initialize entity registration

## TDD Implementation

### Red Phase - Write Failing Tests First

```java
// src/test/java/com/aimobs/entity/AiControlledWolfEntityTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class AiControlledWolfEntityTest {
    
    private ServerWorld testWorld;
    private AiControlledWolfEntity testWolf;
    
    @BeforeEach
    void setUp() {
        testWorld = mock(ServerWorld.class);
        testWolf = new AiControlledWolfEntity(ModEntities.AI_CONTROLLED_WOLF, testWorld);
    }
    
    @Test
    void shouldCreateWolfWithIdleState() {
        assertEquals(AIState.IDLE, testWolf.getCurrentState());
    }
    
    @Test
    void shouldHaveEmptyCommandQueueInitially() {
        assertTrue(testWolf.getCommandQueue().isEmpty());
    }
    
    @Test
    void shouldNotHaveDefaultWolfGoals() {
        // Test that default Wolf AI goals are removed
        assertFalse(hasGoalOfType(testWolf, FollowOwnerGoal.class));
        assertFalse(hasGoalOfType(testWolf, SitGoal.class));
        assertFalse(hasGoalOfType(testWolf, WanderAroundFarGoal.class));
    }
    
    @Test
    void shouldHaveEssentialGoalsOnly() {
        // Test that essential goals remain
        assertTrue(hasGoalOfType(testWolf, SwimGoal.class));
        assertTrue(hasGoalOfType(testWolf, EscapeDangerGoal.class));
    }
    
    @Test
    void shouldHaveControllableGoal() {
        assertTrue(hasGoalOfType(testWolf, ControllableGoal.class));
    }
    
    @Test
    void shouldAcceptCommands() {
        AICommand testCommand = new TestCommand();
        testWolf.executeCommand(testCommand);
        
        assertEquals(AIState.BUSY, testWolf.getCurrentState());
        assertFalse(testWolf.getCommandQueue().isEmpty());
    }
    
    @Test
    void shouldStopCurrentCommand() {
        testWolf.executeCommand(new TestCommand());
        testWolf.stopCurrentCommand();
        
        assertEquals(AIState.IDLE, testWolf.getCurrentState());
    }
    
    private boolean hasGoalOfType(MobEntity entity, Class<?> goalType) {
        return entity.getGoalSelector().getGoals().stream()
            .anyMatch(goal -> goalType.isInstance(goal.getGoal()));
    }
}
```

```java
// src/test/java/com/aimobs/entity/ModEntitiesTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class ModEntitiesTest {
    
    @Test
    void shouldRegisterAiControlledWolfEntity() {
        assertNotNull(ModEntities.AI_CONTROLLED_WOLF);
        assertEquals("ai_controlled_wolf", 
            ModEntities.AI_CONTROLLED_WOLF.getId().getPath());
    }
    
    @Test
    void shouldHaveCorrectEntityAttributes() {
        EntityType<AiControlledWolfEntity> entityType = ModEntities.AI_CONTROLLED_WOLF;
        
        assertTrue(entityType.isSpawnableFarFromPlayer());
        assertEquals(EntityCategory.CREATURE, entityType.getSpawnGroup());
    }
    
    @Test
    void shouldSpawnWolfSuccessfully() {
        ServerWorld world = mock(ServerWorld.class);
        AiControlledWolfEntity wolf = ModEntities.AI_CONTROLLED_WOLF.create(world);
        
        assertNotNull(wolf);
        assertInstanceOf(AiControlledWolfEntity.class, wolf);
    }
}
```

### Green Phase - Implement Minimal Code

```java
// src/main/java/com/aimobs/entity/AiControlledWolfEntity.java
public class AiControlledWolfEntity extends WolfEntity implements CommandExecutor {
    private final Queue<AICommand> commandQueue = new LinkedList<>();
    private AICommand currentCommand = null;
    private AIState currentState = AIState.IDLE;
    
    public AiControlledWolfEntity(EntityType<? extends WolfEntity> entityType, World world) {
        super(entityType, world);
    }
    
    @Override
    protected void initGoals() {
        // Remove default goals, add only essential ones
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 2.2));
        // Add controllable goal system
        this.goalSelector.add(10, new ControllableGoal(this));
    }
    
    @Override
    public void executeCommand(AICommand command) {
        commandQueue.offer(command);
        if (currentState == AIState.IDLE) {
            processNextCommand();
        }
    }
    
    @Override
    public void stopCurrentCommand() {
        currentCommand = null;
        currentState = AIState.IDLE;
        commandQueue.clear();
    }
    
    @Override
    public AIState getCurrentState() {
        return currentState;
    }
    
    @Override
    public Queue<AICommand> getCommandQueue() {
        return new LinkedList<>(commandQueue);
    }
    
    private void processNextCommand() {
        if (!commandQueue.isEmpty()) {
            currentCommand = commandQueue.poll();
            currentState = AIState.BUSY;
            // Command execution logic will be implemented in future tasks
        }
    }
}
```

### Refactor Phase - Improve Implementation

## Implementation Details

### Command Interface
```java
public interface CommandExecutor {
    void executeCommand(AICommand command);
    void stopCurrentCommand();
    AIState getCurrentState();
    Queue<AICommand> getCommandQueue();
}
```

### AI States
- `IDLE`: Default state, no active commands
- `MOVING`: Executing movement command
- `INTERACTING`: Executing interaction command
- `BUSY`: Command in progress, cannot accept new commands

## Validation Criteria

### TDD Test Success
- [ ] All unit tests pass (`./gradlew test`)
- [ ] Entity creation tests pass
- [ ] AI goal override tests pass
- [ ] Command interface tests pass
- [ ] Entity registration tests pass

### Entity Functionality
- [ ] Custom Wolf spawns successfully in world
- [ ] Wolf maintains proper Wolf model and animations
- [ ] Wolf does not exhibit default wandering behavior
- [ ] Wolf remains stationary when no commands given
- [ ] Entity appears in F3 debug as custom type

### AI System
- [ ] Default AI goals are successfully disabled
- [ ] Wolf does not wander, sit, or follow owner behaviors
- [ ] Essential goals (swimming, panic) still function
- [ ] Command queue system works for basic method calls
- [ ] State management tracks current activity

### Registration and Spawning
- [ ] Entity type registers without errors
- [ ] Spawn egg appears in creative inventory
- [ ] Spawn command works for testing
- [ ] Entity saves/loads properly with world
- [ ] No conflicts with existing Fox entities

### Integration
- [ ] Mod loads without errors after entity addition
- [ ] Entity registration completes during mod initialization
- [ ] Multiple controlled Wolf entities can coexist
- [ ] Entity despawning/cleanup works properly

## Testing Instructions

### TDD Workflow
1. **Red Phase**:
   ```bash
   # Run tests (should fail initially)
   ./gradlew test --tests "*.entity.*"
   ```

2. **Green Phase**:
   ```bash
   # Implement code to make tests pass
   ./gradlew test --tests "*.entity.*"
   ```

3. **Refactor Phase**:
   ```bash
   # Improve code while keeping tests green
   ./gradlew test --tests "*.entity.*"
   ```

### Integration Testing
4. **Fabric Test Mod**:
   ```bash
   # Test in full Minecraft environment
   ./gradlew runTestmod
   ```

5. **Basic Spawn Test**:
   ```java
   // In-game command test
   /spawn_ai_wolf
   // Or use spawn egg from creative inventory
   ```

6. **Behavior Test**:
   - Spawn controlled Wolf near player
   - Verify Wolf doesn't move autonomously
   - Confirm Wolf maintains idle state
   - Test essential behaviors (swimming in water)

7. **Method Call Test**:
   ```java
   // Direct method testing for future integration
   AiControlledWolfEntity wolf = // get spawned wolf
   wolf.executeCommand(new TestCommand());
   assert(wolf.getCurrentState() == AIState.BUSY);
   ```

## Success Criteria
The task is complete when:
- All TDD tests pass and provide comprehensive coverage
- Custom Wolf entity spawns and appears identical to normal Wolf
- All default autonomous AI behaviors are disabled
- Wolf remains idle until commanded
- Command interface is ready for future integration
- Entity registration and spawning work reliably
- No errors or conflicts with existing game systems
- Fabric Test Mod environment validates functionality

## Dependencies
- Task 1: Basic Fabric Mod Setup (completed)

## Integration Notes
This entity will later integrate with:
- WebSocket command receiver (Task 3)
- Movement command system (Task 4)
- Interaction command system (Task 5)
- Visual feedback system (Task 6)

## Troubleshooting
Common issues and solutions:
- **Test failures**: Check mock setup and Fabric test environment
- **Entity not spawning**: Check entity registration and attributes
- **Default AI still active**: Verify goal selector override in tests
- **Model/texture issues**: Ensure proper entity type inheritance
- **Crashes on spawn**: Check entity initialization and required components
- **Save/load problems**: Verify entity data serialization
- **TDD setup**: Ensure JUnit extensions and Fabric test dependencies correct