# Task 5: Interaction Command System

## Overview
Implement interaction commands that allow the Wolf to perform actions beyond movement, such as attacking, defending, and collecting items based on AI instructions.

## Context
Extend the Wolf's capabilities with interaction behaviors as specified in the PRD. These commands enable the Wolf to interact with the world, other entities, and items based on voice commands processed through the AI system. Commands include attack, defend, collect, and conversational interactions.

## Technical Requirements
- **Interaction Types**: Attack, defend, collect, and communication commands
- **Target System**: Entity, block, and item targeting with smart resolution
- **Inventory**: Item collection and management for the Wolf entity
- **Combat**: Safe combat mechanics with appropriate limitations
- **Integration**: Commands from WebSocket queue with movement coordination
- **Testing Framework**: JUnit 5 + Fabric Test Mod for interaction testing
- **TDD Approach**: Test-driven development for all interaction functionality

## Entry Point
Starting with the movement command system and previous tasks, implement interaction commands that allow the Wolf to perform complex actions in the Minecraft world, processing commands from the WebSocket connection and coordinating with the movement system.

## Requirements

### Interaction Command Processing
- Parse interaction commands from WebSocket command queue
- Validate targets and action feasibility
- Coordinate with movement system for positioning
- Handle command conflicts and prioritization
- Provide detailed execution feedback

### Combat System Implementation
- **Attack commands**: Target hostile mobs, specific entities
- Safe combat mechanics (no player damage, limited targets)
- Combat state management and disengagement
- Damage dealing appropriate to Wolf entity capabilities
- Combat range and positioning logic

### Collection System Implementation
- **Item collection**: Pick up specified items in area
- Inventory management for collected items
- Item type filtering and smart selection
- Collection radius and pathfinding to items
- Drop item functionality for inventory management

### Defense System Implementation
- **Area defense**: Guard specific locations from hostile mobs
- Patrol behavior within defined boundaries
- Threat detection and response
- Defense mode state management
- Coordinate with attack system for threat response

### Communication Commands
- **Status reporting**: "What do you see?", "How are you?"
- **Conversational responses**: General communication handling
- Integration with future text-to-speech system
- Context-aware responses based on Wolf state and surroundings

## Deliverables

### Required Files

1. **src/main/java/com/aimobs/ai/InteractionController.java**
   - Main interaction command processing
   - Command routing and validation
   - State coordination with movement system
   - Integration with WebSocket commands

2. **src/main/java/com/aimobs/ai/goal/AttackTargetGoal.java**
   - Custom attack behavior for specific targets
   - Safe combat mechanics
   - Target validation and range checking
   - Combat state management

3. **src/main/java/com/aimobs/ai/goal/CollectItemsGoal.java**
   - Item collection behavior
   - Pathfinding to nearby items
   - Item type filtering
   - Inventory management

4. **src/main/java/com/aimobs/ai/goal/DefendAreaGoal.java**
   - Area defense patrol behavior
   - Threat detection and response
   - Boundary enforcement
   - Coordinate with attack goals

5. **src/main/java/com/aimobs/ai/TargetResolver.java** (update)
   - Entity targeting for combat
   - Item targeting for collection
   - Area definition for defense
   - Smart target selection algorithms

6. **src/main/java/com/aimobs/ai/InteractionState.java**
   - Interaction state management
   - Action prioritization
   - Conflict resolution

7. **src/main/java/com/aimobs/inventory/WolfInventoryManager.java**
   - Custom inventory system for Wolf
   - Item storage and retrieval
   - Capacity management
   - Item dropping logic

### Test Files

8. **src/test/java/com/aimobs/ai/InteractionControllerTest.java**
   - TDD tests for interaction command processing
   - Command routing and validation tests
   - State coordination tests

9. **src/test/java/com/aimobs/ai/goal/AttackTargetGoalTest.java**
   - Unit tests for attack behavior
   - Combat mechanics tests
   - Target validation tests

10. **src/test/java/com/aimobs/ai/goal/CollectItemsGoalTest.java**
    - Unit tests for item collection
    - Inventory management tests
    - Item filtering tests

11. **src/test/java/com/aimobs/ai/goal/DefendAreaGoalTest.java**
    - Unit tests for area defense
    - Threat detection tests
    - Patrol behavior tests

12. **src/test/java/com/aimobs/inventory/WolfInventoryManagerTest.java**
    - Inventory system tests
    - Item storage and retrieval tests
    - Capacity management tests

13. **src/testmod/java/com/aimobs/testmod/InteractionTestMod.java**
    - In-game interaction testing environment
    - Mock entities and items setup

## TDD Implementation

### Red Phase - Write Failing Tests First

```java
// src/test/java/com/aimobs/ai/InteractionControllerTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class InteractionControllerTest {
    
    private InteractionController interactionController;
    private AiControlledWolfEntity mockWolf;
    private ServerWorld mockWorld;
    private ServerPlayerEntity mockPlayer;
    private MovementController mockMovementController;
    
    @BeforeEach
    void setUp() {
        mockWorld = mock(ServerWorld.class);
        mockPlayer = mock(ServerPlayerEntity.class);
        mockWolf = mock(AiControlledWolfEntity.class);
        mockMovementController = mock(MovementController.class);
        
        when(mockWolf.getWorld()).thenReturn(mockWorld);
        when(mockWolf.getGoalSelector()).thenReturn(mock(GoalSelector.class));
        when(mockWorld.getClosestPlayer(any(), anyDouble())).thenReturn(mockPlayer);
        when(mockPlayer.getPos()).thenReturn(new Vec3d(10, 64, 10));
        
        interactionController = new InteractionController(mockWolf, mockMovementController);
    }
    
    @Test
    void shouldProcessAttackCommand() {
        CommandMessage attackCommand = createAttackCommand("zombie");
        ZombieEntity mockZombie = mock(ZombieEntity.class);
        when(mockWorld.getEntitiesByClass(eq(ZombieEntity.class), any(), any()))
            .thenReturn(List.of(mockZombie));
        
        interactionController.executeInteractionCommand(attackCommand);
        
        assertEquals(InteractionState.ATTACKING, interactionController.getCurrentState());
        verify(mockWolf.getGoalSelector()).add(eq(5), any(AttackTargetGoal.class));
    }
    
    @Test
    void shouldProcessCollectCommand() {
        CommandMessage collectCommand = createCollectCommand("wood", 10);
        
        interactionController.executeInteractionCommand(collectCommand);
        
        assertEquals(InteractionState.COLLECTING, interactionController.getCurrentState());
        verify(mockWolf.getGoalSelector()).add(eq(5), any(CollectItemsGoal.class));
    }
    
    @Test
    void shouldProcessDefendCommand() {
        CommandMessage defendCommand = createDefendCommand("here", 15);
        when(mockWolf.getBlockPos()).thenReturn(new BlockPos(0, 64, 0));
        
        interactionController.executeInteractionCommand(defendCommand);
        
        assertEquals(InteractionState.DEFENDING, interactionController.getCurrentState());
        verify(mockWolf.getGoalSelector()).add(eq(5), any(DefendAreaGoal.class));
    }
    
    @Test
    void shouldHandleCommunicationCommand() {
        CommandMessage speakCommand = createSpeakCommand("What do you see?");
        
        interactionController.executeInteractionCommand(speakCommand);
        
        // Should generate status response
        assertEquals(InteractionState.IDLE, interactionController.getCurrentState());
    }
    
    @Test
    void shouldCoordinateWithMovementSystem() {
        CommandMessage attackCommand = createAttackCommand("zombie");
        ZombieEntity mockZombie = mock(ZombieEntity.class);
        when(mockZombie.getPos()).thenReturn(new Vec3d(5, 64, 5));
        when(mockWorld.getEntitiesByClass(eq(ZombieEntity.class), any(), any()))
            .thenReturn(List.of(mockZombie));
        
        interactionController.executeInteractionCommand(attackCommand);
        
        verify(mockMovementController).coordinateForInteraction(any(Vec3d.class));
    }
    
    @Test
    void shouldRejectInvalidInteractionCommands() {
        CommandMessage invalidCommand = createInvalidCommand();
        
        interactionController.executeInteractionCommand(invalidCommand);
        
        assertEquals(InteractionState.IDLE, interactionController.getCurrentState());
        verify(mockWolf.getGoalSelector(), never()).add(anyInt(), any());
    }
    
    private CommandMessage createAttackCommand(String target) {
        CommandMessage command = new CommandMessage();
        command.setType("command");
        CommandData data = new CommandData();
        data.setAction("attack");
        data.setParameters(Map.of("target", target));
        command.setData(data);
        return command;
    }
    
    // Additional helper methods...
}
```

### Green Phase - Implement Minimal Code

```java
// src/main/java/com/aimobs/ai/InteractionController.java
public class InteractionController {
    private final AiControlledWolfEntity entity;
    private final MovementController movementController;
    private InteractionState currentState = InteractionState.IDLE;
    private Goal currentInteractionGoal;
    
    public InteractionController(AiControlledWolfEntity entity) {
        this(entity, null);
    }
    
    public InteractionController(AiControlledWolfEntity entity, MovementController movementController) {
        this.entity = entity;
        this.movementController = movementController;
    }
    
    public void executeInteractionCommand(CommandMessage command) {
        if (!isValidInteractionCommand(command)) {
            return;
        }
        
        stopCurrentInteraction();
        
        switch (command.getData().getAction()) {
            case "attack":
                handleAttackCommand(command);
                break;
            case "collect":
                handleCollectCommand(command);
                break;
            case "defend":
                handleDefendCommand(command);
                break;
            case "speak":
                handleCommunicationCommand(command);
                break;
        }
    }
    
    public InteractionState getCurrentState() {
        return currentState;
    }
    
    private boolean isValidInteractionCommand(CommandMessage command) {
        return command != null && 
               command.getData() != null && 
               command.getData().getAction() != null;
    }
}
```

### Refactor Phase - Improve Implementation

### Integration Updates

14. **AiControlledWolfEntity.java** (update)
    - Add InteractionController integration
    - Update AI goal priorities
    - Add interaction command handlers
    - Inventory system integration

15. **MovementController.java** (update)
    - Coordinate with interaction commands
    - Position Wolf for optimal interactions
    - Handle movement-interaction conflicts

## Implementation Details

### Interaction Controller
```java
public class InteractionController {
    private final AiControlledWolfEntity entity;
    private final MovementController movementController;
    private InteractionState currentState = InteractionState.IDLE;
    
    public void executeInteractionCommand(CommandMessage command) {
        switch (command.getData().getAction()) {
            case "attack":
                handleAttackCommand(command);
                break;
            case "collect":
                handleCollectCommand(command);
                break;
            case "defend":
                handleDefendCommand(command);
                break;
            case "speak":
                handleCommunicationCommand(command);
                break;
        }
    }
}
```

### Attack Goal Implementation
```java
public class AttackTargetGoal extends Goal {
    private final AiControlledWolfEntity wolf;
    private LivingEntity target;
    private final double attackRange = 2.0;
    
    @Override
    public boolean canStart() {
        return target != null && target.isAlive() && 
               wolf.distanceTo(target) <= attackRange * 3;
    }
    
    @Override
    public void start() {
        wolf.setTarget(target);
        wolf.getNavigation().startMovingTo(target, 1.2);
    }
}
```

### Collection System
```java
public class CollectItemsGoal extends Goal {
    private final AiControlledWolfEntity wolf;
    private final String itemType;
    private final double collectionRadius;
    private ItemEntity targetItem;
    
    private List<ItemEntity> findNearbyItems() {
        // Find items within radius matching type filter
    }
}
```

### Defense Area Management
```java
public class DefendAreaGoal extends Goal {
    private final AiControlledWolfEntity wolf;
    private final BlockPos centerPos;
    private final double radius;
    private LivingEntity currentThreat;
    
    private void patrolArea() {
        // Patrol within defined radius
        // Detect hostile entities
        // Switch to attack mode when threat found
    }
}
```

## Command Format Support

### Attack Commands
```json
{
  "action": "attack",
  "parameters": {
    "target": "zombie|skeleton|specified_entity",
    "duration": 30,
    "aggressive": false
  },
  "context": {}
}
```

### Collection Commands
```json
{
  "action": "collect",
  "parameters": {
    "item": "wood|stone|apple|all",
    "radius": 10,
    "max_items": 20
  },
  "context": {}
}
```

### Defense Commands
```json
{
  "action": "defend",
  "parameters": {
    "area": "here|player|coordinates",
    "radius": 15,
    "duration": 300
  },
  "context": {}
}
```

## Validation Criteria

### Attack System
- [ ] Wolf attacks specified hostile mobs appropriately
- [ ] Attack range and targeting work correctly
- [ ] Combat state transitions properly (engage/disengage)
- [ ] No inappropriate targeting (players, peaceful mobs)
- [ ] Attack commands can be cancelled with "stop"

### Collection System
- [ ] Wolf collects specified item types in area
- [ ] Pathfinding to items works correctly
- [ ] Item filtering by type functions properly
- [ ] Inventory management prevents overflow
- [ ] Collection radius respects specified parameters

### Defense System
- [ ] Wolf patrols defined area boundaries
- [ ] Detects and responds to hostile threats
- [ ] Coordinates defense with attack system
- [ ] Maintains defensive position appropriately
- [ ] Defense mode can be cancelled or updated

### Communication System
- [ ] Responds to status inquiries appropriately
- [ ] Provides contextual information about surroundings
- [ ] Handles conversational commands gracefully
- [ ] Integrates with future text-to-speech system
- [ ] Response content matches Wolf's current state

### Integration
- [ ] Commands process from WebSocket queue correctly
- [ ] Coordinates with movement system without conflicts
- [ ] State management handles multiple simultaneous commands
- [ ] Error handling for impossible or invalid commands
- [ ] Performance acceptable with multiple active interactions

## Testing Instructions

1. **Attack Command Test**:
   ```json
   {
     "type": "command",
     "data": {
       "action": "attack",
       "parameters": {"target": "zombie"},
       "context": {}
     }
   }
   ```

2. **Collection Test**:
   ```json
   {
     "type": "command",
     "data": {
       "action": "collect", 
       "parameters": {"item": "wood", "radius": 10},
       "context": {}
     }
   }
   ```

3. **Defense Test**:
   ```json
   {
     "type": "command",
     "data": {
       "action": "defend",
       "parameters": {"area": "here", "radius": 20},
       "context": {}
     }
   }
   ```

4. **Integration Test**:
   - Test command combinations (move then attack)
   - Test command interruption (stop during collection)
   - Test multiple entities with different commands
   - Test resource cleanup after command completion

## Success Criteria
The task is complete when:
- All interaction command types function correctly
- Wolf can attack appropriate targets safely
- Item collection works with inventory management
- Area defense patrols and responds to threats
- Commands integrate seamlessly with movement system
- State management handles complex scenarios
- Performance remains acceptable with active interactions
- Error handling prevents crashes and provides feedback

## Dependencies
- Task 1: Basic Fabric Mod Setup (completed)
- Task 2: Custom Wolf Entity Implementation (completed)
- Task 3: WebSocket Client Integration (completed)
- Task 4: Movement Command System (completed)

## Integration Notes
This interaction system will integrate with:
- Visual feedback system (Task 6) - interaction status indicators
- Future text-to-speech system for communication responses
- Future AI personality system for contextual responses

## Safety Considerations
- No player damage from Wolf attacks
- Limited targeting to appropriate entity types
- Inventory management prevents item duplication
- Defense behavior doesn't interfere with player activities
- Proper cleanup prevents entity conflicts

## Troubleshooting
Common issues and solutions:
- **Combat targeting errors**: Verify entity type filtering and range checking
- **Collection failures**: Check item entity detection and pathfinding
- **Defense conflicts**: Ensure proper threat detection and response coordination
- **State management issues**: Verify proper cleanup and state transitions
- **Performance problems**: Optimize target detection and goal execution frequency