# Task 4: Movement Command System

## Overview
Implement movement commands that allow the AI-controlled Wolf to navigate the world based on received instructions from the WebSocket connection.

## Context
Process movement commands from the WebSocket connection and translate them into Wolf entity movement. Commands include "move to location", "follow player", "stop", and "come here" as specified in the PRD. The system must integrate with Minecraft's pathfinding and navigation systems.

## Technical Requirements
- **Navigation**: Minecraft pathfinding integration
- **Commands**: move, follow, stop, comeHere as per PRD Appendix A
- **Targeting**: Coordinate-based and entity-based movement
- **Performance**: Efficient pathfinding with reasonable CPU usage
- **Integration**: Consume commands from WebSocket queue (Task 3)
- **Testing Framework**: JUnit 5 + Fabric Test Mod for movement testing
- **TDD Approach**: Test-driven development for all movement functionality

## Entry Point
Starting with the custom Wolf entity and WebSocket integration from previous tasks, implement a movement command system that processes commands from the command queue and executes Wolf navigation using Minecraft's built-in pathfinding.

## Requirements

### Movement Command Processing
- Parse movement commands from WebSocket command queue
- Validate command parameters and targets
- Convert natural language targets to coordinates
- Handle invalid or impossible movement requests
- Provide execution feedback and status updates

### Pathfinding Integration
- Use Minecraft's built-in navigation system
- Handle obstacles and terrain navigation
- Implement efficient path recalculation
- Support different movement speeds
- Handle pathfinding failures gracefully

### Command Types Implementation
Support movement commands as specified in PRD:
- **"Go to [object]"** → move to nearest matching object/coordinates
- **"Follow me"** → continuously follow the player
- **"Stop"** → cease current movement immediately
- **"Come here"** → move to player's current location

### State Management
- Track current movement state (idle, moving, following, etc.)
- Handle command interruption and overrides
- Manage movement completion detection
- Provide progress feedback for long movements

## Deliverables

### Required Files

1. **src/main/java/com/aimobs/ai/MovementController.java**
   - Main movement command processing
   - Command parsing and validation
   - Integration with entity navigation
   - State management

2. **src/main/java/com/aimobs/ai/goal/MoveToLocationGoal.java**
   - Custom AI goal for coordinate-based movement
   - Pathfinding integration
   - Completion detection

3. **src/main/java/com/aimobs/ai/goal/FollowPlayerGoal.java**
   - Custom AI goal for player following
   - Distance management
   - Continuous tracking

4. **src/main/java/com/aimobs/ai/goal/StopMovementGoal.java**
   - Command to halt all movement
   - State cleanup
   - Immediate execution

5. **src/main/java/com/aimobs/ai/TargetResolver.java**
   - Convert string targets to coordinates
   - Object/block finding logic
   - Nearest target selection

6. **src/main/java/com/aimobs/ai/MovementState.java**
   - Movement state enumeration
   - State transition management
   - Status reporting

### Test Files

7. **src/test/java/com/aimobs/ai/MovementControllerTest.java**
   - TDD tests for movement command processing
   - Pathfinding integration tests
   - State management tests

8. **src/test/java/com/aimobs/ai/goal/MoveToLocationGoalTest.java**
   - Unit tests for move-to-location AI goal
   - Pathfinding behavior tests

9. **src/test/java/com/aimobs/ai/goal/FollowPlayerGoalTest.java**
   - Unit tests for player following AI goal
   - Distance management tests

10. **src/test/java/com/aimobs/ai/TargetResolverTest.java**
    - Target resolution and validation tests
    - Coordinate parsing tests

11. **src/testmod/java/com/aimobs/testmod/MovementTestMod.java**
    - In-game movement testing environment
    - Mock player and world setup

### Integration Updates

12. **AiControlledWolfEntity.java** (update)
    - Add MovementController integration
    - Update AI goal system
    - Add movement command handlers

13. **WebSocketManager.java** (update)
    - Route movement commands to controller
    - Send movement status updates

## TDD Implementation

### Red Phase - Write Failing Tests First

```java
// src/test/java/com/aimobs/ai/MovementControllerTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class MovementControllerTest {
    
    private MovementController movementController;
    private AiControlledWolfEntity mockWolf;
    private ServerWorld mockWorld;
    private ServerPlayerEntity mockPlayer;
    
    @BeforeEach
    void setUp() {
        mockWorld = mock(ServerWorld.class);
        mockPlayer = mock(ServerPlayerEntity.class);
        mockWolf = mock(AiControlledWolfEntity.class);
        
        when(mockWolf.getWorld()).thenReturn(mockWorld);
        when(mockWorld.getClosestPlayer(any(), anyDouble())).thenReturn(mockPlayer);
        when(mockPlayer.getPos()).thenReturn(new Vec3d(10, 64, 10));
        
        movementController = new MovementController(mockWolf);
    }
    
    @Test
    void shouldProcessMoveCommand() {
        CommandMessage moveCommand = createMoveCommand(5, 64, 5);
        
        movementController.executeMovementCommand(moveCommand);
        
        assertEquals(MovementState.MOVING, movementController.getCurrentState());
        verify(mockWolf).getNavigation();
    }
    
    @Test
    void shouldProcessFollowCommand() {
        CommandMessage followCommand = createFollowCommand();
        
        movementController.executeMovementCommand(followCommand);
        
        assertEquals(MovementState.FOLLOWING, movementController.getCurrentState());
    }
    
    @Test
    void shouldStopMovementOnStopCommand() {
        // Set up wolf in moving state
        movementController.executeMovementCommand(createMoveCommand(5, 64, 5));
        
        CommandMessage stopCommand = createStopCommand();
        movementController.executeMovementCommand(stopCommand);
        
        assertEquals(MovementState.IDLE, movementController.getCurrentState());
    }
    
    @Test
    void shouldHandleComeHereCommand() {
        CommandMessage comeHereCommand = createComeHereCommand();
        
        movementController.executeMovementCommand(comeHereCommand);
        
        assertEquals(MovementState.MOVING, movementController.getCurrentState());
        // Should move to player position
    }
    
    @Test
    void shouldRejectInvalidCommands() {
        CommandMessage invalidCommand = createInvalidCommand();
        
        movementController.executeMovementCommand(invalidCommand);
        
        assertEquals(MovementState.IDLE, movementController.getCurrentState());
    }
    
    @Test
    void shouldInterruptCurrentMovementWithNewCommand() {
        movementController.executeMovementCommand(createMoveCommand(5, 64, 5));
        assertEquals(MovementState.MOVING, movementController.getCurrentState());
        
        movementController.executeMovementCommand(createStopCommand());
        
        assertEquals(MovementState.IDLE, movementController.getCurrentState());
    }
    
    private CommandMessage createMoveCommand(int x, int y, int z) {
        CommandMessage command = new CommandMessage();
        command.setType("command");
        CommandData data = new CommandData();
        data.setAction("move");
        data.setParameters(Map.of("x", x, "y", y, "z", z));
        command.setData(data);
        return command;
    }
    
    private CommandMessage createFollowCommand() {
        CommandMessage command = new CommandMessage();
        command.setType("command");
        CommandData data = new CommandData();
        data.setAction("follow");
        data.setParameters(Map.of("target", "player"));
        command.setData(data);
        return command;
    }
    
    // Additional helper methods...
}
```

```java
// src/test/java/com/aimobs/ai/goal/MoveToLocationGoalTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class MoveToLocationGoalTest {
    
    private MoveToLocationGoal moveGoal;
    private AiControlledWolfEntity mockWolf;
    private EntityNavigation mockNavigation;
    
    @BeforeEach
    void setUp() {
        mockWolf = mock(AiControlledWolfEntity.class);
        mockNavigation = mock(EntityNavigation.class);
        
        when(mockWolf.getNavigation()).thenReturn(mockNavigation);
        when(mockWolf.getPos()).thenReturn(new Vec3d(0, 64, 0));
        
        BlockPos target = new BlockPos(10, 64, 10);
        moveGoal = new MoveToLocationGoal(mockWolf, target, 1.0);
    }
    
    @Test
    void shouldStartWhenTargetIsSet() {
        assertTrue(moveGoal.canStart());
    }
    
    @Test
    void shouldNotStartWhenTooFarFromTarget() {
        BlockPos farTarget = new BlockPos(1000, 64, 1000);
        moveGoal = new MoveToLocationGoal(mockWolf, farTarget, 1.0);
        
        assertFalse(moveGoal.canStart());
    }
    
    @Test
    void shouldStartNavigationWhenGoalStarts() {
        moveGoal.start();
        
        verify(mockNavigation).startMovingTo(10.0, 64.0, 10.0, 1.0);
    }
    
    @Test
    void shouldStopWhenTargetReached() {
        when(mockWolf.getPos()).thenReturn(new Vec3d(10, 64, 10)); // At target
        when(mockNavigation.isIdle()).thenReturn(true);
        
        assertFalse(moveGoal.shouldContinue());
    }
    
    @Test
    void shouldContinueWhenStillMoving() {
        when(mockNavigation.isIdle()).thenReturn(false);
        
        assertTrue(moveGoal.shouldContinue());
    }
}
```

### Green Phase - Implement Minimal Code

```java
// src/main/java/com/aimobs/ai/MovementController.java
public class MovementController {
    private final AiControlledWolfEntity entity;
    private MovementState currentState = MovementState.IDLE;
    private Goal currentMovementGoal;
    
    public MovementController(AiControlledWolfEntity entity) {
        this.entity = entity;
    }
    
    public void executeMovementCommand(CommandMessage command) {
        if (!isValidMovementCommand(command)) {
            return;
        }
        
        stopCurrentMovement();
        
        switch (command.getData().getAction()) {
            case "move":
                handleMoveCommand(command);
                break;
            case "follow":
                handleFollowCommand(command);
                break;
            case "stop":
                handleStopCommand();
                break;
            case "comeHere":
                handleComeHereCommand(command);
                break;
        }
    }
    
    private void handleMoveCommand(CommandMessage command) {
        Map<String, Object> params = command.getData().getParameters();
        int x = ((Number) params.get("x")).intValue();
        int y = ((Number) params.get("y")).intValue();
        int z = ((Number) params.get("z")).intValue();
        
        BlockPos target = new BlockPos(x, y, z);
        currentMovementGoal = new MoveToLocationGoal(entity, target, 1.0);
        entity.getGoalSelector().add(5, currentMovementGoal);
        currentState = MovementState.MOVING;
    }
    
    private void handleFollowCommand(CommandMessage command) {
        PlayerEntity player = entity.getWorld().getClosestPlayer(entity, 50.0);
        if (player != null) {
            currentMovementGoal = new FollowPlayerGoal(entity, player, 1.2, 3.0, 10.0);
            entity.getGoalSelector().add(5, currentMovementGoal);
            currentState = MovementState.FOLLOWING;
        }
    }
    
    private void handleStopCommand() {
        stopCurrentMovement();
    }
    
    private void handleComeHereCommand(CommandMessage command) {
        PlayerEntity player = entity.getWorld().getClosestPlayer(entity, 50.0);
        if (player != null) {
            BlockPos playerPos = player.getBlockPos();
            currentMovementGoal = new MoveToLocationGoal(entity, playerPos, 1.2);
            entity.getGoalSelector().add(5, currentMovementGoal);
            currentState = MovementState.MOVING;
        }
    }
    
    private void stopCurrentMovement() {
        if (currentMovementGoal != null) {
            entity.getGoalSelector().remove(currentMovementGoal);
            currentMovementGoal = null;
        }
        entity.getNavigation().stop();
        currentState = MovementState.IDLE;
    }
    
    public MovementState getCurrentState() {
        return currentState;
    }
    
    private boolean isValidMovementCommand(CommandMessage command) {
        return command != null && 
               command.getData() != null && 
               command.getData().getAction() != null;
    }
}
```

### Refactor Phase - Improve Implementation

## Implementation Details
```

### Movement Goals
```java
public class MoveToLocationGoal extends Goal {
    private final MobEntity mob;
    private final BlockPos target;
    private final double speed;
    
    @Override
    public boolean canStart() {
        return target != null && !mob.getNavigation().isIdle();
    }
    
    @Override
    public void start() {
        mob.getNavigation().startMovingTo(target.getX(), target.getY(), target.getZ(), speed);
    }
}
```

### Target Resolution
```java
public class TargetResolver {
    public static BlockPos resolveTarget(String target, World world, Vec3d origin) {
        // Implementation for converting strings like "tree", "house" to coordinates
        // Support for relative positions: "north", "10 blocks away"
        // Integration with block/entity finding
    }
}
```

### Command Format Support
Movement commands from WebSocket:
```json
{
  "action": "move",
  "parameters": {
    "target": "tree",
    "speed": 1.0,
    "distance": 10
  },
  "context": {
    "entity_id": "fox_1"
  }
}
```

## Validation Criteria

### TDD Test Success
- [ ] All unit tests pass (`./gradlew test --tests "*.ai.*"`)
- [ ] Movement controller tests pass
- [ ] AI goal tests pass
- [ ] Target resolution tests pass
- [ ] Integration tests with mock entities pass

### Command Processing
- [ ] Processes movement commands from WebSocket queue
- [ ] Validates command parameters correctly
- [ ] Handles invalid commands without crashing
- [ ] Provides appropriate error messages for invalid targets
- [ ] Queues multiple commands properly

### Navigation Implementation
- [ ] Wolf moves to specified coordinates accurately
- [ ] Pathfinding navigates around obstacles
- [ ] Movement speed is controllable via commands
- [ ] Wolf stops movement when "stop" command received
- [ ] Path recalculation works when target moves

### Command Types
- [ ] **Move command**: Wolf moves to specified target/coordinates
- [ ] **Follow command**: Wolf continuously follows player
- [ ] **Stop command**: Wolf immediately ceases movement
- [ ] **Come here command**: Wolf moves to player's current position
- [ ] Command interruption works (new command stops current)

### State Management
- [ ] Movement state updates correctly during execution
- [ ] State transitions are consistent and logical
- [ ] Multiple entities can have independent movement states
- [ ] State persists appropriately across game sessions

### Performance
- [ ] Pathfinding doesn't cause significant lag
- [ ] Multiple moving entities perform acceptably
- [ ] Long-distance movement doesn't block game
- [ ] Resource cleanup prevents memory leaks

## Testing Instructions

### TDD Workflow
1. **Red Phase**:
   ```bash
   # Run tests (should fail initially)
   ./gradlew test --tests "*.ai.*"
   ```

2. **Green Phase**:
   ```bash
   # Implement code to make tests pass
   ./gradlew test --tests "*.ai.*"
   ```

3. **Refactor Phase**:
   ```bash
   # Improve code while keeping tests green
   ./gradlew test --tests "*.ai.*"
   ```

### Integration Testing
4. **Fabric Test Mod**:
   ```bash
   # Test with mock Wolf entities and worlds
   ./gradlew runTestmod
   ```

5. **Basic Movement Test**:
   ```json
   // Send via WebSocket
   {
     "type": "command",
     "timestamp": "2025-01-19T10:00:00Z",
     "data": {
       "action": "move",
       "parameters": {"x": 100, "y": 64, "z": 100},
       "context": {}
     }
   }
   ```

6. **Follow Test**:
   ```json
   {
     "type": "command", 
     "data": {
       "action": "follow",
       "parameters": {"target": "player"},
       "context": {}
     }
   }
   ```

7. **Target Resolution Test**:
   - Test coordinate-based movement
   - Test relative movement ("north", "nearby tree")
   - Test invalid targets
   - Test unreachable locations

8. **Pathfinding Test**:
   - Place Wolf behind obstacles
   - Test movement across different terrain
   - Test vertical movement (stairs, cliffs)
   - Test water navigation

## Success Criteria
The task is complete when:
- All TDD tests pass and provide comprehensive coverage
- All movement command types work correctly
- Wolf navigates using Minecraft pathfinding efficiently
- Commands are processed from WebSocket queue
- Target resolution works for various input types
- State management tracks movement status accurately
- Performance is acceptable with multiple entities
- Integration with previous tasks is seamless
- Mock-based testing validates all functionality

## Dependencies
- Task 1: Basic Fabric Mod Setup (completed)
- Task 2: Custom Wolf Entity Implementation (completed) 
- Task 3: WebSocket Client Integration (completed)

## Integration Notes
This movement system will integrate with:
- Interaction command system (Task 5) - coordinate movement for interactions
- Visual feedback system (Task 6) - movement status indicators
- Future voice command parsing for natural language targets

## Performance Considerations
- Pathfinding frequency optimization
- Target validation before starting navigation
- Cleanup of completed/failed movement goals
- Efficient state checking and updates

## Troubleshooting
Common issues and solutions:
- **Pathfinding failures**: Check target reachability and terrain
- **Performance issues**: Optimize pathfinding frequency and distance
- **State synchronization**: Ensure proper thread safety with WebSocket
- **Command queuing**: Verify proper command prioritization and execution
- **Navigation stuck**: Implement timeout and retry mechanisms