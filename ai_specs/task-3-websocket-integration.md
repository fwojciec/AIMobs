# Task 3: WebSocket Client Integration

## Overview
Add WebSocket client functionality to the mod to receive external commands for Wolf control from the AI processing server.

## Context
The mod needs to connect to an external WebSocket server (port 8080) to receive AI-processed commands. Commands will be in JSON format according to the PRD specification and need to be parsed, validated, and queued for execution by the controlled Wolf entities.

## Technical Requirements
- **WebSocket Library**: Java-WebSocket or similar lightweight library
- **Connection Target**: localhost:8080 (configurable)
- **Message Format**: JSON as specified in PRD
- **Connection Management**: Auto-reconnection, heartbeat, error handling
- **Threading**: Non-blocking operation with Minecraft's main thread
- **Testing Framework**: JUnit 5 + Fabric Test Mod for WebSocket testing
- **TDD Approach**: Test-driven development for all networking functionality

## Entry Point
Starting with the basic Fabric mod and custom Wolf entity from previous tasks, add WebSocket client capability that can connect to an external server, receive JSON commands, and prepare them for execution by the Wolf entity system.

## Requirements

### WebSocket Client Implementation
- Establish WebSocket connection to configurable server endpoint
- Implement connection lifecycle management (connect, disconnect, reconnect)
- Handle connection states and status reporting
- Non-blocking operation with proper thread management
- Graceful degradation when server unavailable

### Message Handling
- Parse incoming JSON messages according to PRD format
- Validate command structure and parameters
- Queue commands for execution by entity system
- Handle malformed or invalid messages gracefully
- Implement message acknowledgment system

### Command Structure Support
Support for command types as specified in PRD:
```json
{
  "type": "command|response|status",
  "timestamp": "ISO-8601",
  "data": {
    "action": "move|attack|speak|status",
    "parameters": {},
    "context": {}
  }
}
```

### Configuration System
- Configurable WebSocket server URL and port
- Connection timeout and retry settings
- Debug logging controls
- Enable/disable WebSocket functionality

## Deliverables

### Required Files

1. **src/main/java/com/aimobs/network/WebSocketManager.java**
   - Main WebSocket client implementation
   - Connection management and lifecycle
   - Message parsing and validation
   - Error handling and reconnection logic

2. **src/main/java/com/aimobs/network/CommandMessage.java**
   - JSON message data structure
   - Serialization/deserialization
   - Message validation

3. **src/main/java/com/aimobs/network/CommandQueue.java**
   - Thread-safe command queue
   - Command prioritization
   - Queue management and cleanup

4. **src/main/java/com/aimobs/config/NetworkConfig.java**
   - Configuration management
   - Default settings
   - Runtime configuration updates

5. **src/main/java/com/aimobs/network/WebSocketListener.java**
   - WebSocket event handlers
   - Connection state management
   - Message routing

### Build Configuration Updates

6. **build.gradle** (update)
   - Add WebSocket client dependency
   - JSON parsing library (Gson or Jackson)

### Test Files

7. **src/test/java/com/aimobs/network/WebSocketManagerTest.java**
   - TDD tests for WebSocket connection management
   - Message parsing and validation tests
   - Error handling and reconnection tests

8. **src/test/java/com/aimobs/network/CommandMessageTest.java**
   - JSON serialization/deserialization tests
   - Message validation tests

9. **src/test/java/com/aimobs/network/CommandQueueTest.java**
   - Thread-safe queue operation tests
   - Command prioritization tests

10. **src/testmod/java/com/aimobs/testmod/NetworkTestMod.java**
    - Mock WebSocket server for testing
    - Integration test environment

### Integration Updates

11. **AiMobsMod.java** (update)
    - Initialize WebSocket manager
    - Configuration loading
    - Graceful shutdown handling

## TDD Implementation

### Red Phase - Write Failing Tests First

```java
// src/test/java/com/aimobs/network/WebSocketManagerTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class WebSocketManagerTest {
    
    private WebSocketManager webSocketManager;
    private MockWebSocketServer mockServer;
    private CommandQueue mockCommandQueue;
    
    @BeforeEach
    void setUp() {
        mockCommandQueue = mock(CommandQueue.class);
        mockServer = new MockWebSocketServer(8080);
        webSocketManager = new WebSocketManager(mockCommandQueue);
    }
    
    @AfterEach
    void tearDown() {
        mockServer.stop();
        webSocketManager.disconnect();
    }
    
    @Test
    void shouldConnectToWebSocketServer() {
        mockServer.start();
        
        webSocketManager.connect("ws://localhost:8080");
        
        assertTrue(webSocketManager.isConnected());
    }
    
    @Test
    void shouldHandleConnectionFailureGracefully() {
        // Server not started
        webSocketManager.connect("ws://localhost:8080");
        
        assertFalse(webSocketManager.isConnected());
        // Should not throw exceptions
    }
    
    @Test
    void shouldParseValidJsonMessages() {
        String validJson = """
            {
              "type": "command",
              "timestamp": "2025-01-19T10:00:00Z",
              "data": {
                "action": "move",
                "parameters": {"x": 10, "y": 64, "z": 10},
                "context": {}
              }
            }
            """;
        
        webSocketManager.handleMessage(validJson);
        
        verify(mockCommandQueue).offer(any(CommandMessage.class));
    }
    
    @Test
    void shouldRejectMalformedJsonMessages() {
        String invalidJson = "{ invalid json }";
        
        webSocketManager.handleMessage(invalidJson);
        
        verify(mockCommandQueue, never()).offer(any());
    }
    
    @Test
    void shouldReconnectAfterConnectionLoss() {
        mockServer.start();
        webSocketManager.connect("ws://localhost:8080");
        assertTrue(webSocketManager.isConnected());
        
        mockServer.simulateDisconnection();
        
        // Wait for reconnection attempt
        await().atMost(5, SECONDS).until(() -> webSocketManager.isConnected());
    }
    
    @Test
    void shouldNotBlockMinecraftMainThread() {
        CompletableFuture<Void> connectFuture = CompletableFuture.runAsync(() -> 
            webSocketManager.connect("ws://localhost:8080"));
        
        assertDoesNotThrow(() -> connectFuture.get(1, SECONDS));
    }
}
```

```java
// src/test/java/com/aimobs/network/CommandMessageTest.java
@ExtendWith(FabricLoaderJunitExtension.class)
class CommandMessageTest {
    
    private Gson gson;
    
    @BeforeEach
    void setUp() {
        gson = new Gson();
    }
    
    @Test
    void shouldDeserializeValidCommandMessage() {
        String json = """
            {
              "type": "command",
              "timestamp": "2025-01-19T10:00:00Z",
              "data": {
                "action": "move",
                "parameters": {"x": 10, "y": 64, "z": 10},
                "context": {}
              }
            }
            """;
        
        CommandMessage message = gson.fromJson(json, CommandMessage.class);
        
        assertEquals("command", message.getType());
        assertEquals("move", message.getData().getAction());
        assertEquals(10, message.getData().getParameters().get("x"));
    }
    
    @Test
    void shouldValidateRequiredFields() {
        CommandMessage message = new CommandMessage();
        
        assertFalse(message.isValid());
    }
    
    @Test
    void shouldValidateActionTypes() {
        CommandMessage message = createValidMessage();
        message.getData().setAction("invalid_action");
        
        assertFalse(message.isValid());
    }
    
    @Test
    void shouldSerializeToJson() {
        CommandMessage message = createValidMessage();
        
        String json = gson.toJson(message);
        
        assertThat(json).contains("\"type\":\"command\"");
        assertThat(json).contains("\"action\":\"move\"");
    }
    
    private CommandMessage createValidMessage() {
        CommandMessage message = new CommandMessage();
        message.setType("command");
        message.setTimestamp("2025-01-19T10:00:00Z");
        
        CommandData data = new CommandData();
        data.setAction("move");
        data.setParameters(Map.of("x", 10, "y", 64, "z", 10));
        data.setContext(Map.of());
        
        message.setData(data);
        return message;
    }
}
```

### Green Phase - Implement Minimal Code

```java
// src/main/java/com/aimobs/network/WebSocketManager.java
public class WebSocketManager {
    private WebSocketClient client;
    private final CommandQueue commandQueue;
    private volatile boolean connected = false;
    private final Gson gson = new Gson();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    
    public WebSocketManager(CommandQueue commandQueue) {
        this.commandQueue = commandQueue;
    }
    
    public void connect(String serverUrl) {
        executor.execute(() -> {
            try {
                URI serverUri = new URI(serverUrl);
                client = new WebSocketClient(serverUri) {
                    @Override
                    public void onOpen(ServerHandshake handshake) {
                        connected = true;
                        AiMobsMod.LOGGER.info("WebSocket connected");
                    }
                    
                    @Override
                    public void onMessage(String message) {
                        handleMessage(message);
                    }
                    
                    @Override
                    public void onClose(int code, String reason, boolean remote) {
                        connected = false;
                        scheduleReconnection();
                    }
                    
                    @Override
                    public void onError(Exception ex) {
                        AiMobsMod.LOGGER.error("WebSocket error", ex);
                    }
                };
                client.connect();
            } catch (Exception e) {
                AiMobsMod.LOGGER.error("Failed to connect to WebSocket", e);
            }
        });
    }
    
    public void handleMessage(String message) {
        try {
            CommandMessage commandMessage = gson.fromJson(message, CommandMessage.class);
            if (commandMessage.isValid()) {
                commandQueue.offer(commandMessage);
            } else {
                AiMobsMod.LOGGER.warn("Invalid command message received");
            }
        } catch (JsonSyntaxException e) {
            AiMobsMod.LOGGER.warn("Malformed JSON message: " + message);
        }
    }
    
    public boolean isConnected() {
        return connected && client != null && client.isOpen();
    }
    
    private void scheduleReconnection() {
        executor.schedule(() -> {
            if (!connected && client != null) {
                client.reconnect();
            }
        }, 5, TimeUnit.SECONDS);
    }
}
```

### Refactor Phase - Improve Implementation

## Implementation Details
```

### Command Message Format
```java
public class CommandMessage {
    private String type;
    private String timestamp;
    private CommandData data;
    
    public static class CommandData {
        private String action;
        private Map<String, Object> parameters;
        private Map<String, Object> context;
    }
}
```

### Thread Safety
- Use Minecraft's main thread for entity modifications
- WebSocket operations on separate thread
- Thread-safe command queue with proper synchronization
- Proper cleanup on mod shutdown

## Validation Criteria

### TDD Test Success
- [ ] All unit tests pass (`./gradlew test --tests "*.network.*"`)
- [ ] WebSocket connection tests pass
- [ ] Message parsing and validation tests pass
- [ ] Thread safety tests pass
- [ ] Mock server integration tests pass

### Connection Management
- [ ] Successfully connects to WebSocket server on localhost:8080
- [ ] Handles server unavailable gracefully (no crashes)
- [ ] Automatically reconnects after connection loss
- [ ] Reports connection status accurately
- [ ] Properly disconnects on mod shutdown

### Message Processing
- [ ] Receives and parses JSON messages correctly
- [ ] Validates message structure according to PRD format
- [ ] Handles malformed JSON without crashing
- [ ] Queues valid commands for processing
- [ ] Logs invalid commands with appropriate detail

### Integration
- [ ] WebSocket manager initializes with mod startup
- [ ] No blocking of Minecraft main thread
- [ ] Commands appear in queue after WebSocket reception
- [ ] Configuration system allows server URL changes
- [ ] Debug logging provides useful information

### Error Handling
- [ ] Network errors don't crash the game
- [ ] JSON parsing errors are handled gracefully
- [ ] Server disconnection doesn't break mod functionality
- [ ] Recovery from various error states works properly

## Testing Instructions

### TDD Workflow
1. **Red Phase**:
   ```bash
   # Run tests (should fail initially)
   ./gradlew test --tests "*.network.*"
   ```

2. **Green Phase**:
   ```bash
   # Implement code to make tests pass
   ./gradlew test --tests "*.network.*"
   ```

3. **Refactor Phase**:
   ```bash
   # Improve code while keeping tests green
   ./gradlew test --tests "*.network.*"
   ```

### Integration Testing
4. **Fabric Test Mod**:
   ```bash
   # Test with mock WebSocket server
   ./gradlew runTestmod
   ```

5. **Connection Test**:
   ```bash
   # Start simple WebSocket server for testing
   # Verify mod connects successfully
   # Check connection status in logs
   ```

6. **Message Test**:
   ```json
   // Send test message via WebSocket
   {
     "type": "command",
     "timestamp": "2025-01-19T10:00:00Z",
     "data": {
       "action": "move",
       "parameters": {"x": 10, "y": 64, "z": 10},
       "context": {"entity_id": "test_wolf"}
     }
   }
   ```

7. **Error Handling Test**:
   - Disconnect server while mod running
   - Send malformed JSON messages
   - Send invalid command structures
   - Test reconnection after server restart

8. **Performance Test**:
   - Send multiple rapid commands
   - Verify no main thread blocking
   - Check memory usage over time

## Success Criteria
The task is complete when:
- All TDD tests pass and provide comprehensive coverage
- WebSocket client connects to server successfully
- JSON messages are parsed and validated correctly
- Commands are queued for entity execution
- Connection management (reconnect, error handling) works reliably
- No blocking of Minecraft main thread
- Configuration system allows runtime adjustments
- Comprehensive error handling prevents crashes
- Mock WebSocket server tests validate integration

## Dependencies
- Task 1: Basic Fabric Mod Setup (completed)
- Task 2: Custom Wolf Entity Implementation (completed)
- External WebSocket server (will be developed separately)

## Integration Notes
This WebSocket client will integrate with:
- Movement command system (Task 4) - commands from queue
- Interaction command system (Task 5) - commands from queue  
- Visual feedback system (Task 6) - status updates
- Future AI processing server providing commands

## Mock Testing
For independent testing without external server:
```java
// Mock WebSocket server for testing
public class MockWebSocketServer {
    // Simple test server sending predefined commands
    // Useful for validating client implementation
}
```

## Troubleshooting
Common issues and solutions:
- **Connection refused**: Check server availability and port
- **JSON parsing errors**: Validate message format against PRD spec
- **Thread deadlocks**: Ensure proper thread separation
- **Memory leaks**: Verify queue cleanup and connection disposal
- **Reconnection failures**: Check retry logic and timeout settings