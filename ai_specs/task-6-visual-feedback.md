# Task 6: Visual Feedback System

## Overview
Add visual and audio feedback to indicate when commands are received, executed, or failed, providing clear communication about Wolf status and actions to the player.

## Context
Users need clear feedback about Wolf command execution status as specified in the PRD. Implement particle effects, sounds, chat messages, and visual indicators to communicate Wolf state and actions clearly. This system should provide immediate feedback for command reception and ongoing status during command execution.

## Technical Requirements
- **Particle Effects**: Custom particles for different command states
- **Sound Effects**: Audio feedback for actions and state changes
- **Chat Messages**: Text feedback for status and errors
- **Visual Indicators**: Above-entity status displays and effects
- **Integration**: Feedback for all command types from previous tasks
- **Testing Framework**: JUnit 5 + Fabric Test Mod for feedback testing
- **TDD Approach**: Test-driven development for all feedback functionality

## Entry Point
Starting with the complete command system (movement and interaction), implement a comprehensive feedback system that provides visual, audio, and text indicators for all Wolf activities, making the AI control system intuitive and informative for users.

## Requirements

### Visual Feedback System
- Particle effects for command execution states
- Above-entity status indicators (floating text, icons)
- Entity outline or glow effects for different states
- Visual paths or targeting indicators
- Color-coded feedback for different command types

### Audio Feedback System
- Sound effects for command reception
- Action-specific audio (movement, attack, collection sounds)
- Success/failure audio cues
- Ambient status sounds for ongoing activities
- Volume and distance-appropriate audio

### Text Feedback System
- Chat messages for command status
- Error messages for failed commands
- Detailed status reports on request
- Command history display
- Debug information for development

### State Indication System
- Real-time status display above Wolf entity
- Activity progress indicators
- Target highlighting and indication
- Path visualization for movement commands
- Inventory status display for collection activities

## Deliverables

### Required Files

1. **src/main/java/com/aimobs/feedback/FeedbackManager.java**
   - Central feedback coordination
   - Message routing to appropriate systems
   - State-based feedback logic
   - Integration with all command controllers

2. **src/main/java/com/aimobs/feedback/ParticleEffects.java**
   - Custom particle effect definitions
   - Particle spawning and management
   - State-specific particle systems
   - Performance-optimized particle rendering

3. **src/main/java/com/aimobs/feedback/SoundManager.java**
   - Sound effect management
   - Audio cue coordination
   - Volume and distance calculations
   - Sound resource management

4. **src/main/java/com/aimobs/feedback/ChatMessageHandler.java**
   - Chat message formatting and sending
   - Message categorization and filtering
   - Player-specific message routing
   - Debug message controls

5. **src/main/java/com/aimobs/feedback/StatusDisplay.java**
   - Above-entity status rendering
   - Progress indicators
   - State icon management
   - Visual status updates

6. **src/main/java/com/aimobs/feedback/TargetIndicator.java**
   - Target highlighting system
   - Path visualization
   - Area marking for defense commands
   - Interactive feedback elements

### Resource Files

7. **src/main/resources/assets/aimobs/sounds/command_received.ogg**
8. **src/main/resources/assets/aimobs/sounds/command_completed.ogg**
9. **src/main/resources/assets/aimobs/sounds/command_failed.ogg**
10. **src/main/resources/assets/aimobs/sounds/wolf_moving.ogg**
11. **src/main/resources/assets/aimobs/sounds/wolf_attacking.ogg**
12. **src/main/resources/assets/aimobs/sounds/wolf_collecting.ogg**

13. **src/main/resources/assets/aimobs/textures/gui/status_icons.png**
14. **src/main/resources/assets/aimobs/lang/en_us.json** (update)

### Test Files

15. **src/test/java/com/aimobs/feedback/FeedbackManagerTest.java**
    - TDD tests for feedback coordination
    - Command state feedback tests
    - Integration testing with controllers

16. **src/test/java/com/aimobs/feedback/ParticleEffectsTest.java**
    - Unit tests for particle spawning
    - Particle effect validation tests
    - Performance testing for particle systems

17. **src/test/java/com/aimobs/feedback/SoundManagerTest.java**
    - Audio playback tests
    - Volume and distance calculation tests
    - Sound resource management tests

18. **src/test/java/com/aimobs/feedback/ChatMessageHandlerTest.java**
    - Message formatting and sending tests
    - Player-specific routing tests
    - Message filtering and categorization tests

19. **src/test/java/com/aimobs/feedback/StatusDisplayTest.java**
    - Status rendering tests
    - Progress indicator tests
    - Visual update validation tests

20. **src/testmod/java/com/aimobs/testmod/FeedbackTestMod.java**
    - In-game feedback testing environment
    - Visual and audio feedback validation
    - Integration testing with mock commands

### Integration Updates

21. **All Controllers** (update)
    - Add feedback calls to all command processors
    - Integrate status reporting with feedback system
    - Add error state feedback

## TDD Implementation

### Red Phase - Write Failing Tests First

```java
// src/test/java/com/aimobs/feedback/FeedbackManagerTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class FeedbackManagerTest {
    
    private FeedbackManager feedbackManager;
    private ParticleEffects mockParticles;
    private SoundManager mockSounds;
    private ChatMessageHandler mockChat;
    private StatusDisplay mockStatusDisplay;
    private AiControlledWolfEntity mockWolf;
    private ServerWorld mockWorld;
    
    @BeforeEach
    void setUp() {
        mockParticles = mock(ParticleEffects.class);
        mockSounds = mock(SoundManager.class);
        mockChat = mock(ChatMessageHandler.class);
        mockStatusDisplay = mock(StatusDisplay.class);
        mockWolf = mock(AiControlledWolfEntity.class);
        mockWorld = mock(ServerWorld.class);
        
        when(mockWolf.getWorld()).thenReturn(mockWorld);
        when(mockWolf.getX()).thenReturn(0.0);
        when(mockWolf.getY()).thenReturn(64.0);
        when(mockWolf.getZ()).thenReturn(0.0);
        
        feedbackManager = new FeedbackManager(mockParticles, mockSounds, mockChat, mockStatusDisplay);
    }
    
    @Test
    void shouldProvideCommandReceivedFeedback() {
        CommandMessage command = createMoveCommand();
        
        feedbackManager.onCommandReceived(mockWolf, command);
        
        verify(mockParticles).spawnCommandReceivedEffect(mockWolf);
        verify(mockSounds).playCommandReceivedSound(mockWolf);
        verify(mockChat).sendMessage(contains("Command received"));
        verify(mockStatusDisplay).updateStatus(mockWolf, "Processing...");
    }
    
    @Test
    void shouldProvideCommandExecutingFeedback() {
        feedbackManager.onCommandExecuting(mockWolf, "move");
        
        verify(mockParticles).spawnExecutionEffect(mockWolf, "move");
        verify(mockStatusDisplay).updateStatus(mockWolf, "Executing: move");
    }
    
    @Test
    void shouldProvideCommandCompletedFeedback() {
        feedbackManager.onCommandCompleted(mockWolf, "move");
        
        verify(mockParticles).spawnSuccessEffect(mockWolf);
        verify(mockSounds).playSuccessSound(mockWolf);
        verify(mockChat).sendMessage(contains("Completed"));
        verify(mockStatusDisplay).updateStatus(mockWolf, "Idle");
    }
    
    @Test
    void shouldProvideCommandFailedFeedback() {
        String reason = "Target unreachable";
        
        feedbackManager.onCommandFailed(mockWolf, "move", reason);
        
        verify(mockParticles).spawnFailureEffect(mockWolf);
        verify(mockSounds).playFailureSound(mockWolf);
        verify(mockChat).sendError(contains("Failed move"));
        verify(mockStatusDisplay).updateStatus(mockWolf, "Error");
    }
    
    @Test
    void shouldProvideMovementSpecificFeedback() {
        BlockPos target = new BlockPos(10, 64, 10);
        
        feedbackManager.onMovementStarted(mockWolf, target);
        
        verify(mockParticles).spawnTargetIndicator(target);
        verify(mockStatusDisplay).showMovementProgress(mockWolf, target);
    }
    
    @Test
    void shouldProvideAttackSpecificFeedback() {
        LivingEntity target = mock(ZombieEntity.class);
        
        feedbackManager.onAttackStarted(mockWolf, target);
        
        verify(mockParticles).spawnCombatEffect(mockWolf);
        verify(mockSounds).playCombatSound(mockWolf);
        verify(mockStatusDisplay).showCombatStatus(mockWolf, target);
    }
    
    @Test
    void shouldHandleMultipleWolvesIndependently() {
        AiControlledWolfEntity wolf2 = mock(AiControlledWolfEntity.class);
        when(wolf2.getWorld()).thenReturn(mockWorld);
        
        feedbackManager.onCommandReceived(mockWolf, createMoveCommand());
        feedbackManager.onCommandReceived(wolf2, createAttackCommand());
        
        verify(mockParticles, times(2)).spawnCommandReceivedEffect(any());
        verify(mockStatusDisplay).updateStatus(mockWolf, "Processing...");
        verify(mockStatusDisplay).updateStatus(wolf2, "Processing...");
    }
    
    private CommandMessage createMoveCommand() {
        CommandMessage command = new CommandMessage();
        command.setType("command");
        CommandData data = new CommandData();
        data.setAction("move");
        data.setParameters(Map.of("x", 10, "y", 64, "z", 10));
        command.setData(data);
        return command;
    }
    
    private CommandMessage createAttackCommand() {
        CommandMessage command = new CommandMessage();
        command.setType("command");
        CommandData data = new CommandData();
        data.setAction("attack");
        data.setParameters(Map.of("target", "zombie"));
        command.setData(data);
        return command;
    }
}
```

```java
// src/test/java/com/aimobs/feedback/ParticleEffectsTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class ParticleEffectsTest {
    
    private ParticleEffects particleEffects;
    private ServerWorld mockWorld;
    private AiControlledWolfEntity mockWolf;
    
    @BeforeEach
    void setUp() {
        mockWorld = mock(ServerWorld.class);
        mockWolf = mock(AiControlledWolfEntity.class);
        
        when(mockWolf.getWorld()).thenReturn(mockWorld);
        when(mockWolf.getX()).thenReturn(5.0);
        when(mockWolf.getY()).thenReturn(64.0);
        when(mockWolf.getZ()).thenReturn(5.0);
        
        particleEffects = new ParticleEffects();
    }
    
    @Test
    void shouldSpawnCommandReceivedParticles() {
        particleEffects.spawnCommandReceivedEffect(mockWolf);
        
        verify(mockWorld).spawnParticles(
            eq(ParticleTypes.ENCHANT),
            eq(5.0), eq(65.0), eq(5.0),
            anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }
    
    @Test
    void shouldSpawnSuccessParticles() {
        particleEffects.spawnSuccessEffect(mockWolf);
        
        verify(mockWorld).spawnParticles(
            eq(ParticleTypes.HAPPY_VILLAGER),
            eq(5.0), eq(65.0), eq(5.0),
            anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }
    
    @Test
    void shouldSpawnFailureParticles() {
        particleEffects.spawnFailureEffect(mockWolf);
        
        verify(mockWorld).spawnParticles(
            eq(ParticleTypes.ANGRY_VILLAGER),
            eq(5.0), eq(65.0), eq(5.0),
            anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }
    
    @Test
    void shouldSpawnMovementTrail() {
        particleEffects.spawnMovementTrail(mockWolf);
        
        verify(mockWorld).spawnParticles(
            eq(ParticleTypes.CLOUD),
            anyDouble(), anyDouble(), anyDouble(),
            anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }
    
    @Test
    void shouldSpawnTargetIndicator() {
        BlockPos target = new BlockPos(10, 64, 10);
        
        particleEffects.spawnTargetIndicator(mockWorld, target);
        
        verify(mockWorld).spawnParticles(
            eq(ParticleTypes.END_ROD),
            eq(10.5), eq(65.0), eq(10.5),
            anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }
    
    @Test
    void shouldSpawnActionSpecificParticles() {
        // Test attack particles
        particleEffects.spawnCombatEffect(mockWolf);
        verify(mockWorld).spawnParticles(eq(ParticleTypes.SWEEP_ATTACK), anyDouble(), anyDouble(), anyDouble(),
            anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        
        // Test collection particles
        particleEffects.spawnCollectionEffect(mockWolf);
        verify(mockWorld).spawnParticles(eq(ParticleTypes.ITEM_PICKUP), anyDouble(), anyDouble(), anyDouble(),
            anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }
    
    @Test
    void shouldLimitParticleCount() {
        // Spawn many particles rapidly
        for (int i = 0; i < 100; i++) {
            particleEffects.spawnCommandReceivedEffect(mockWolf);
        }
        
        // Should not spawn more than the limit
        verify(mockWorld, atMost(20)).spawnParticles(any(), anyDouble(), anyDouble(), anyDouble(),
            anyInt(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }
}
```

```java
// src/test/java/com/aimobs/feedback/SoundManagerTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class SoundManagerTest {
    
    private SoundManager soundManager;
    private ServerWorld mockWorld;
    private AiControlledWolfEntity mockWolf;
    private ServerPlayerEntity mockPlayer;
    
    @BeforeEach
    void setUp() {
        mockWorld = mock(ServerWorld.class);
        mockWolf = mock(AiControlledWolfEntity.class);
        mockPlayer = mock(ServerPlayerEntity.class);
        
        when(mockWolf.getWorld()).thenReturn(mockWorld);
        when(mockWolf.getPos()).thenReturn(new Vec3d(0, 64, 0));
        when(mockPlayer.getPos()).thenReturn(new Vec3d(10, 64, 10));
        
        soundManager = new SoundManager();
    }
    
    @Test
    void shouldPlayCommandReceivedSound() {
        soundManager.playCommandReceivedSound(mockWolf);
        
        verify(mockWorld).playSound(
            isNull(),
            eq(0.0), eq(64.0), eq(0.0),
            any(SoundEvent.class),
            eq(SoundCategory.NEUTRAL),
            anyFloat(), anyFloat()
        );
    }
    
    @Test
    void shouldPlaySuccessSound() {
        soundManager.playSuccessSound(mockWolf);
        
        verify(mockWorld).playSound(
            isNull(),
            anyDouble(), anyDouble(), anyDouble(),
            any(SoundEvent.class),
            eq(SoundCategory.NEUTRAL),
            anyFloat(), anyFloat()
        );
    }
    
    @Test
    void shouldPlayFailureSound() {
        soundManager.playFailureSound(mockWolf);
        
        verify(mockWorld).playSound(
            isNull(),
            anyDouble(), anyDouble(), anyDouble(),
            any(SoundEvent.class),
            eq(SoundCategory.NEUTRAL),
            anyFloat(), anyFloat()
        );
    }
    
    @Test
    void shouldCalculateVolumeByDistance() {
        // Test close distance
        when(mockPlayer.getPos()).thenReturn(new Vec3d(1, 64, 1));
        float closeVolume = soundManager.calculateVolume(mockWolf, mockPlayer);
        
        // Test far distance
        when(mockPlayer.getPos()).thenReturn(new Vec3d(50, 64, 50));
        float farVolume = soundManager.calculateVolume(mockWolf, mockPlayer);
        
        assertTrue(closeVolume > farVolume);
        assertTrue(closeVolume <= 1.0f);
        assertTrue(farVolume >= 0.0f);
    }
    
    @Test
    void shouldPlayActionSpecificSounds() {
        soundManager.playCombatSound(mockWolf);
        soundManager.playMovementSound(mockWolf);
        soundManager.playCollectionSound(mockWolf);
        
        // Verify different sounds are played
        verify(mockWorld, times(3)).playSound(
            isNull(),
            anyDouble(), anyDouble(), anyDouble(),
            any(SoundEvent.class),
            any(SoundCategory.class),
            anyFloat(), anyFloat()
        );
    }
    
    @Test
    void shouldRespectSoundSettings() {
        soundManager.setEnabled(false);
        
        soundManager.playCommandReceivedSound(mockWolf);
        
        verify(mockWorld, never()).playSound(any(), anyDouble(), anyDouble(), anyDouble(),
            any(SoundEvent.class), any(SoundCategory.class), anyFloat(), anyFloat());
    }
}
```

```java
// src/test/java/com/aimobs/feedback/ChatMessageHandlerTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class ChatMessageHandlerTest {
    
    private ChatMessageHandler chatHandler;
    private ServerPlayerEntity mockPlayer;
    private MinecraftServer mockServer;
    
    @BeforeEach
    void setUp() {
        mockPlayer = mock(ServerPlayerEntity.class);
        mockServer = mock(MinecraftServer.class);
        
        chatHandler = new ChatMessageHandler(mockServer);
    }
    
    @Test
    void shouldSendInfoMessage() {
        String message = "Command received: move";
        
        chatHandler.sendMessage(mockPlayer, message);
        
        verify(mockPlayer).sendMessage(any(Text.class), eq(false));
    }
    
    @Test
    void shouldSendErrorMessage() {
        String error = "Failed to find target";
        
        chatHandler.sendError(mockPlayer, error);
        
        ArgumentCaptor<Text> textCaptor = ArgumentCaptor.forClass(Text.class);
        verify(mockPlayer).sendMessage(textCaptor.capture(), eq(false));
        
        Text sentText = textCaptor.getValue();
        assertTrue(sentText.getString().contains("Error"));
        assertTrue(sentText.getString().contains(error));
    }
    
    @Test
    void shouldSendSuccessMessage() {
        String action = "move";
        
        chatHandler.sendSuccess(mockPlayer, action);
        
        ArgumentCaptor<Text> textCaptor = ArgumentCaptor.forClass(Text.class);
        verify(mockPlayer).sendMessage(textCaptor.capture(), eq(false));
        
        Text sentText = textCaptor.getValue();
        assertTrue(sentText.getString().contains("Completed"));
        assertTrue(sentText.getString().contains(action));
    }
    
    @Test
    void shouldFormatMessagesCorrectly() {
        String message = chatHandler.formatInfoMessage("Test message");
        
        assertThat(message).contains("[AI Mobs]");
        assertThat(message).contains("Test message");
    }
    
    @Test
    void shouldFilterDebugMessages() {
        chatHandler.setDebugEnabled(false);
        
        chatHandler.sendDebugMessage(mockPlayer, "Debug info");
        
        verify(mockPlayer, never()).sendMessage(any(Text.class), anyBoolean());
    }
    
    @Test
    void shouldBroadcastToAllPlayers() {
        ServerPlayerEntity player2 = mock(ServerPlayerEntity.class);
        when(mockServer.getPlayerManager()).thenReturn(mock(PlayerManager.class));
        when(mockServer.getPlayerManager().getPlayerList()).thenReturn(List.of(mockPlayer, player2));
        
        chatHandler.broadcastMessage("Server message");
        
        verify(mockPlayer).sendMessage(any(Text.class), eq(false));
        verify(player2).sendMessage(any(Text.class), eq(false));
    }
    
    @Test
    void shouldRateLimitMessages() {
        // Send many messages rapidly
        for (int i = 0; i < 20; i++) {
            chatHandler.sendMessage(mockPlayer, "Spam message " + i);
        }
        
        // Should not send all messages due to rate limiting
        verify(mockPlayer, atMost(10)).sendMessage(any(Text.class), anyBoolean());
    }
}
```

```java
// src/test/java/com/aimobs/feedback/StatusDisplayTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class StatusDisplayTest {
    
    private StatusDisplay statusDisplay;
    private AiControlledWolfEntity mockWolf;
    private ClientWorld mockClientWorld;
    
    @BeforeEach
    void setUp() {
        mockWolf = mock(AiControlledWolfEntity.class);
        mockClientWorld = mock(ClientWorld.class);
        
        when(mockWolf.getWorld()).thenReturn(mockClientWorld);
        when(mockWolf.getPos()).thenReturn(new Vec3d(0, 64, 0));
        
        statusDisplay = new StatusDisplay();
    }
    
    @Test
    void shouldUpdateStatusText() {
        String status = "Moving to target";
        
        statusDisplay.updateStatus(mockWolf, status);
        
        assertEquals(status, statusDisplay.getStatus(mockWolf));
    }
    
    @Test
    void shouldShowProgressIndicator() {
        float progress = 0.5f;
        
        statusDisplay.showProgress(mockWolf, progress);
        
        assertEquals(progress, statusDisplay.getProgress(mockWolf), 0.01f);
    }
    
    @Test
    void shouldClearStatusWhenIdle() {
        statusDisplay.updateStatus(mockWolf, "Executing command");
        statusDisplay.clearStatus(mockWolf);
        
        assertNull(statusDisplay.getStatus(mockWolf));
    }
    
    @Test
    void shouldDisplayDifferentStatusIcons() {
        statusDisplay.updateStatus(mockWolf, "Moving", StatusIcon.MOVEMENT);
        assertEquals(StatusIcon.MOVEMENT, statusDisplay.getIcon(mockWolf));
        
        statusDisplay.updateStatus(mockWolf, "Attacking", StatusIcon.COMBAT);
        assertEquals(StatusIcon.COMBAT, statusDisplay.getIcon(mockWolf));
        
        statusDisplay.updateStatus(mockWolf, "Collecting", StatusIcon.COLLECTION);
        assertEquals(StatusIcon.COLLECTION, statusDisplay.getIcon(mockWolf));
    }
    
    @Test
    void shouldHandleMultipleEntities() {
        AiControlledWolfEntity wolf2 = mock(AiControlledWolfEntity.class);
        when(wolf2.getWorld()).thenReturn(mockClientWorld);
        
        statusDisplay.updateStatus(mockWolf, "Moving");
        statusDisplay.updateStatus(wolf2, "Attacking");
        
        assertEquals("Moving", statusDisplay.getStatus(mockWolf));
        assertEquals("Attacking", statusDisplay.getStatus(wolf2));
    }
    
    @Test
    void shouldRenderStatusAboveEntity() {
        statusDisplay.updateStatus(mockWolf, "Test Status");
        
        // Test that render method is called during client tick
        statusDisplay.render(mockWolf, 0.0f);
        
        // Verify status is displayed above entity position
        Vec3d expectedPos = mockWolf.getPos().add(0, 2.5, 0);
        // Additional rendering verification would depend on specific rendering implementation
    }
    
    @Test
    void shouldFadeStatusOverTime() {
        statusDisplay.updateStatus(mockWolf, "Temporary Status");
        
        // Simulate time passing
        statusDisplay.tick();
        
        // Status should still be visible
        assertNotNull(statusDisplay.getStatus(mockWolf));
        
        // Simulate more time passing
        for (int i = 0; i < 100; i++) {
            statusDisplay.tick();
        }
        
        // Status should fade or be removed
        assertTrue(statusDisplay.getAlpha(mockWolf) < 1.0f || statusDisplay.getStatus(mockWolf) == null);
    }
}
```

### Green Phase - Implement Minimal Code

```java
// src/main/java/com/aimobs/feedback/FeedbackManager.java
public class FeedbackManager {
    private final ParticleEffects particles;
    private final SoundManager sounds;
    private final ChatMessageHandler chat;
    private final StatusDisplay statusDisplay;
    
    public FeedbackManager(ParticleEffects particles, SoundManager sounds, 
                          ChatMessageHandler chat, StatusDisplay statusDisplay) {
        this.particles = particles;
        this.sounds = sounds;
        this.chat = chat;
        this.statusDisplay = statusDisplay;
    }
    
    public void onCommandReceived(AiControlledWolfEntity wolf, CommandMessage command) {
        particles.spawnCommandReceivedEffect(wolf);
        sounds.playCommandReceivedSound(wolf);
        chat.sendMessage(getPlayer(wolf), "Command received: " + command.getData().getAction());
        statusDisplay.updateStatus(wolf, "Processing...");
    }
    
    public void onCommandExecuting(AiControlledWolfEntity wolf, String action) {
        particles.spawnExecutionEffect(wolf, action);
        statusDisplay.updateStatus(wolf, "Executing: " + action);
    }
    
    public void onCommandCompleted(AiControlledWolfEntity wolf, String action) {
        particles.spawnSuccessEffect(wolf);
        sounds.playSuccessSound(wolf);
        chat.sendSuccess(getPlayer(wolf), action);
        statusDisplay.updateStatus(wolf, "Idle");
    }
    
    public void onCommandFailed(AiControlledWolfEntity wolf, String action, String reason) {
        particles.spawnFailureEffect(wolf);
        sounds.playFailureSound(wolf);
        chat.sendError(getPlayer(wolf), "Failed " + action + ": " + reason);
        statusDisplay.updateStatus(wolf, "Error");
    }
    
    private ServerPlayerEntity getPlayer(AiControlledWolfEntity wolf) {
        return wolf.getWorld().getClosestPlayer(wolf, 50.0);
    }
}
```

### Refactor Phase - Improve Implementation

## Implementation Details

### Feedback Manager
```java
public class FeedbackManager {
    private final ParticleEffects particles;
    private final SoundManager sounds;
    private final ChatMessageHandler chat;
    private final StatusDisplay statusDisplay;
    
    public void onCommandReceived(AiControlledWolfEntity wolf, CommandMessage command) {
        particles.spawnCommandReceivedEffect(wolf);
        sounds.playCommandReceivedSound(wolf);
        chat.sendMessage("Command received: " + command.getAction());
        statusDisplay.updateStatus(wolf, "Processing...");
    }
    
    public void onCommandExecuting(AiControlledWolfEntity wolf, String action) {
        particles.spawnExecutionEffect(wolf, action);
        statusDisplay.updateStatus(wolf, "Executing: " + action);
    }
    
    public void onCommandCompleted(AiControlledWolfEntity wolf, String action) {
        particles.spawnSuccessEffect(wolf);
        sounds.playSuccessSound(wolf);
        chat.sendMessage("Completed: " + action);
        statusDisplay.updateStatus(wolf, "Idle");
    }
    
    public void onCommandFailed(AiControlledWolfEntity wolf, String action, String reason) {
        particles.spawnFailureEffect(wolf);
        sounds.playFailureSound(wolf);
        chat.sendError("Failed " + action + ": " + reason);
        statusDisplay.updateStatus(wolf, "Error");
    }
}
```

### Particle Effects System
```java
public class ParticleEffects {
    public void spawnCommandReceivedEffect(AiControlledWolfEntity wolf) {
        // Spawn blue sparkle particles around wolf
        wolf.getWorld().addParticle(ParticleTypes.ENCHANT,
            wolf.getX(), wolf.getY() + 1, wolf.getZ(),
            0.0, 0.1, 0.0);
    }
    
    public void spawnMovementTrail(AiControlledWolfEntity wolf) {
        // Spawn trailing particles during movement
    }
    
    public void spawnTargetIndicator(BlockPos target) {
        // Highlight target location with particles
    }
}
```

### Status Display System
```java
public class StatusDisplay {
    public void updateStatus(AiControlledWolfEntity wolf, String status) {
        // Render floating text above wolf
        // Update status icon
        // Color-code based on activity type
    }
    
    public void showProgress(AiControlledWolfEntity wolf, float progress) {
        // Display progress bar for long operations
    }
}
```

## Feedback Types

### Command Reception Feedback
- **Visual**: Blue sparkle particles around Wolf
- **Audio**: Soft chime sound
- **Text**: "Command received: [action]"
- **Status**: "Processing..." above entity

### Movement Feedback
- **Visual**: Trail particles during movement, target highlighting
- **Audio**: Footstep enhancement sounds
- **Text**: "Moving to [target]"
- **Status**: "Moving" with direction arrow

### Attack Feedback
- **Visual**: Red combat particles, target outline
- **Audio**: Combat engagement sounds
- **Text**: "Attacking [target]"
- **Status**: "Combat" with crossed swords icon

### Collection Feedback
- **Visual**: Green collection aura, item highlighting
- **Audio**: Collection pickup sounds
- **Text**: "Collecting [item type]"
- **Status**: "Collecting" with item count

### Defense Feedback
- **Visual**: Yellow patrol area markers, threat indicators
- **Audio**: Alert sounds when threats detected
- **Text**: "Defending area"
- **Status**: "Guarding" with shield icon

### Error Feedback
- **Visual**: Red warning particles
- **Audio**: Error buzz sound
- **Text**: "Error: [detailed message]"
- **Status**: "Error" with warning icon

## Validation Criteria

### TDD Test Success
- [ ] All unit tests pass (`./gradlew test --tests "*.feedback.*"`)
- [ ] Feedback manager tests pass
- [ ] Particle effects tests pass
- [ ] Sound manager tests pass
- [ ] Chat message handler tests pass
- [ ] Status display tests pass
- [ ] Integration tests with Fabric Test Mod pass

### Visual System
- [ ] Particles appear correctly for all command states
- [ ] Status display updates in real-time above Wolf entity
- [ ] Target highlighting works for movement and interaction
- [ ] Visual effects don't cause performance issues
- [ ] Color coding is consistent and intuitive

### Audio System
- [ ] Sound effects play at appropriate times
- [ ] Audio volume scales with distance appropriately
- [ ] No audio conflicts or overlapping issues
- [ ] All command types have distinct audio feedback
- [ ] Sound effects enhance rather than distract from gameplay

### Text System
- [ ] Chat messages appear for all major command events
- [ ] Error messages provide helpful information
- [ ] Message formatting is consistent and readable
- [ ] Debug information can be enabled/disabled
- [ ] Text feedback doesn't spam chat excessively

### Integration
- [ ] All command controllers trigger appropriate feedback
- [ ] Feedback timing aligns with actual command execution
- [ ] Multiple Wolf entities have independent feedback
- [ ] Feedback system doesn't interfere with game performance
- [ ] State transitions trigger correct feedback changes

### User Experience
- [ ] Feedback provides clear understanding of Wolf status
- [ ] Visual indicators are visible and unobtrusive
- [ ] Audio cues are informative without being annoying
- [ ] Error states are clearly communicated
- [ ] Overall feedback enhances the AI control experience

## Testing Instructions

### TDD Workflow
1. **Red Phase**:
   ```bash
   # Run tests (should fail initially)
   ./gradlew test --tests "*.feedback.*"
   ```

2. **Green Phase**:
   ```bash
   # Implement code to make tests pass
   ./gradlew test --tests "*.feedback.*"
   ```

3. **Refactor Phase**:
   ```bash
   # Improve code while keeping tests green
   ./gradlew test --tests "*.feedback.*"
   ```

### Integration Testing
4. **Fabric Test Mod**:
   ```bash
   # Test with mock feedback systems
   ./gradlew runTestmod
   ```

5. **Visual Feedback Test**:
   - Send various commands via WebSocket
   - Verify particles appear for each command state
   - Check status display updates correctly
   - Test multiple Wolf entities simultaneously

2. **Audio Feedback Test**:
   - Test all sound effects with commands
   - Verify volume scaling with distance
   - Check for audio conflicts and overlaps
   - Test audio disable/enable functionality

3. **Text Feedback Test**:
   - Verify chat messages for successful commands
   - Test error message display for invalid commands
   - Check message formatting and readability
   - Test debug information toggle

4. **Integration Test**:
   - Test feedback with movement commands
   - Test feedback with interaction commands
   - Verify feedback during command interruption
   - Test feedback system performance under load

## Success Criteria
The task is complete when:
- All TDD tests pass and provide comprehensive coverage
- All command states provide clear visual feedback
- Audio system enhances user understanding of Wolf activities
- Text messages provide appropriate detail for all scenarios
- Status display accurately reflects Wolf state in real-time
- Feedback system integrates seamlessly with all command types
- Performance impact is minimal and acceptable
- User experience is significantly improved with feedback system
- Error states are clearly communicated and actionable
- Mock-based testing validates all feedback functionality

## Dependencies
- Task 1: Basic Fabric Mod Setup (completed)
- Task 2: Custom Wolf Entity Implementation (completed)
- Task 3: WebSocket Client Integration (completed)
- Task 4: Movement Command System (completed)
- Task 5: Interaction Command System (completed)

## Integration Notes
This feedback system completes the Minecraft mod component and will integrate with:
- Future web interface for command status display
- Text-to-speech system for audio response feedback
- AI personality system for contextual feedback messages

## Performance Considerations
- Particle effect limits and cleanup
- Audio resource management and caching
- Text message rate limiting
- Visual effect LOD (Level of Detail) for distant entities
- Efficient status update frequency

## Troubleshooting
Common issues and solutions:
- **Particle performance**: Implement particle limits and cleanup
- **Audio conflicts**: Proper sound channel management
- **Text spam**: Rate limiting and message consolidation
- **Visual clutter**: Configurable feedback intensity settings
- **Integration timing**: Ensure feedback calls align with actual command states