package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.core.*;
import com.aimobs.network.core.NetworkMessage;
import com.aimobs.test.BaseUnitTest;
import com.aimobs.test.FakeEntityResolverService;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InteractionCommandFactory following TDD approach.
 * Tests command creation logic in isolation.
 */
class InteractionCommandFactoryTest extends BaseUnitTest {
    
    private FakeEntityResolverService fakeEntityResolver;
    
    @BeforeEach
    void setUp() {
        fakeEntityResolver = new FakeEntityResolverService();
    }
    
    @Test
    void shouldCreateAttackCommand() {
        NetworkMessage message = createNetworkMessage("attack", Map.of("target", "zombie"));
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNotNull(command);
        assertInstanceOf(AttackTargetCommand.class, command);
        assertEquals(InteractionType.ATTACK, command.getInteractionType());
        assertTrue(command.requiresPositioning());
        assertEquals("zombie", fakeEntityResolver.getLastResolvedType());
    }
    
    @Test
    void shouldCreateCollectCommand() {
        Map<String, Object> parameters = Map.of(
            "item", "wood",
            "radius", 15.0,
            "max_items", 10
        );
        NetworkMessage message = createNetworkMessage("collect", parameters);
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNotNull(command);
        assertInstanceOf(CollectItemsCommand.class, command);
        
        CollectItemsCommand collectCommand = (CollectItemsCommand) command;
        assertEquals("wood", collectCommand.getItemType());
        assertEquals(15.0, collectCommand.getRadius());
        assertEquals(10, collectCommand.getMaxItems());
        assertEquals(InteractionType.COLLECT, command.getInteractionType());
        assertTrue(command.requiresPositioning());
    }
    
    @Test
    void shouldCreateCollectCommandWithDefaults() {
        NetworkMessage message = createNetworkMessage("collect", Map.of("item", "stone"));
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNotNull(command);
        assertInstanceOf(CollectItemsCommand.class, command);
        
        CollectItemsCommand collectCommand = (CollectItemsCommand) command;
        assertEquals("stone", collectCommand.getItemType());
        assertEquals(10.0, collectCommand.getRadius()); // Default radius
        assertEquals(0, collectCommand.getMaxItems()); // Default max items (unlimited)
    }
    
    @Test
    void shouldCreateDefendCommandForCurrentLocation() {
        Map<String, Object> parameters = Map.of(
            "area", "here",
            "radius", 20.0
        );
        NetworkMessage message = createNetworkMessage("defend", parameters);
        Vec3d entityPos = new Vec3d(10, 64, 15);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNotNull(command);
        assertInstanceOf(DefendAreaCommand.class, command);
        
        DefendAreaCommand defendCommand = (DefendAreaCommand) command;
        assertEquals(new BlockPos(10, 64, 15), defendCommand.getCenterPos());
        assertEquals(20.0, defendCommand.getRadius());
        assertEquals(InteractionType.DEFEND, command.getInteractionType());
        assertTrue(command.requiresPositioning());
    }
    
    @Test
    void shouldCreateDefendCommandForSpecificCoordinates() {
        Map<String, Object> coordinates = Map.of(
            "x", 5,
            "y", 64,
            "z", 8
        );
        Map<String, Object> parameters = Map.of(
            "area", coordinates,
            "radius", 25.0
        );
        NetworkMessage message = createNetworkMessage("defend", parameters);
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNotNull(command);
        assertInstanceOf(DefendAreaCommand.class, command);
        
        DefendAreaCommand defendCommand = (DefendAreaCommand) command;
        assertEquals(new BlockPos(5, 64, 8), defendCommand.getCenterPos());
        assertEquals(25.0, defendCommand.getRadius());
    }
    
    @Test
    void shouldCreateDefendCommandWithDefaults() {
        NetworkMessage message = createNetworkMessage("defend", Map.of());
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNotNull(command);
        assertInstanceOf(DefendAreaCommand.class, command);
        
        DefendAreaCommand defendCommand = (DefendAreaCommand) command;
        assertEquals(new BlockPos(0, 64, 0), defendCommand.getCenterPos());
        assertEquals(15.0, defendCommand.getRadius()); // Default radius
    }
    
    @Test
    void shouldCreateCommunicationCommand() {
        NetworkMessage message = createNetworkMessage("speak", Map.of("message", "Hello, how are you?"));
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNotNull(command);
        assertInstanceOf(CommunicationCommand.class, command);
        
        CommunicationCommand commCommand = (CommunicationCommand) command;
        assertEquals("Hello, how are you?", commCommand.getMessage());
        assertEquals(InteractionType.COMMUNICATE, command.getInteractionType());
        assertFalse(command.requiresPositioning());
    }
    
    @Test
    void shouldCreateCommunicationCommandFromCommunicateAction() {
        NetworkMessage message = createNetworkMessage("communicate", Map.of("message", "Status report"));
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNotNull(command);
        assertInstanceOf(CommunicationCommand.class, command);
        
        CommunicationCommand commCommand = (CommunicationCommand) command;
        assertEquals("Status report", commCommand.getMessage());
    }
    
    @Test
    void shouldReturnNullForInvalidMessage() {
        assertNull(InteractionCommandFactory.createInteractionCommand(null, fakeEntityResolver, new Vec3d(0, 64, 0)));
        
        NetworkMessage invalidMessage = new NetworkMessage();
        assertNull(InteractionCommandFactory.createInteractionCommand(invalidMessage, fakeEntityResolver, new Vec3d(0, 64, 0)));
        
        NetworkMessage messageWithNullData = new NetworkMessage();
        messageWithNullData.setType("command");
        messageWithNullData.setTimestamp(Instant.now().toString());
        assertNull(InteractionCommandFactory.createInteractionCommand(messageWithNullData, fakeEntityResolver, new Vec3d(0, 64, 0)));
    }
    
    @Test
    void shouldReturnNullForUnsupportedAction() {
        NetworkMessage message = createNetworkMessage("invalid_action", Map.of());
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNull(command);
    }
    
    @Test
    void shouldReturnNullForAttackWithNoTarget() {
        NetworkMessage message = createNetworkMessage("attack", Map.of());
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNull(command);
    }
    
    @Test
    void shouldReturnNullForAttackWithNoValidTargets() {
        NetworkMessage message = createNetworkMessage("attack", Map.of("target", "zombie"));
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        // Configure fake resolver to return no entity
        fakeEntityResolver.setShouldReturnEntity(false);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNull(command);
    }
    
    @Test
    void shouldReturnNullForCollectWithNoItemType() {
        NetworkMessage message = createNetworkMessage("collect", Map.of("radius", 10.0));
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNull(command);
    }
    
    @Test
    void shouldReturnNullForCommunicateWithNoMessage() {
        NetworkMessage message = createNetworkMessage("speak", Map.of());
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNull(command);
    }
    
    @Test
    void shouldHandlePriorityParameters() {
        Map<String, Object> parameters = Map.of(
            "item", "wood",
            "priority", 7
        );
        NetworkMessage message = createNetworkMessage("collect", parameters);
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNotNull(command);
        assertEquals(7, command.getPriority());
    }
    
    @Test
    void shouldHandleNumericParametersAsNumbers() {
        Map<String, Object> parameters = Map.of(
            "item", "stone",
            "radius", 12,  // Integer instead of double
            "max_items", 5.0  // Double instead of integer
        );
        NetworkMessage message = createNetworkMessage("collect", parameters);
        Vec3d entityPos = new Vec3d(0, 64, 0);
        
        InteractionCommand command = InteractionCommandFactory.createInteractionCommand(message, fakeEntityResolver, entityPos);
        
        assertNotNull(command);
        assertInstanceOf(CollectItemsCommand.class, command);
        
        CollectItemsCommand collectCommand = (CollectItemsCommand) command;
        assertEquals(12.0, collectCommand.getRadius());
        assertEquals(5, collectCommand.getMaxItems());
    }
    
    private NetworkMessage createNetworkMessage(String action, Map<String, Object> parameters) {
        NetworkMessage message = new NetworkMessage();
        message.setType("command");
        message.setTimestamp(Instant.now().toString());
        
        NetworkMessage.MessageData data = new NetworkMessage.MessageData();
        data.setAction(action);
        data.setParameters(new HashMap<>(parameters));
        data.setContext(new HashMap<>());
        
        message.setData(data);
        
        return message;
    }
}