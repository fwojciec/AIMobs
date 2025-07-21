package com.aimobs.testmod;

import com.aimobs.entity.AiControlledWolfEntity;
import com.aimobs.entity.ai.InteractionService;
import com.aimobs.entity.ai.core.InventoryActions;
import com.aimobs.network.core.NetworkMessage;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.server.command.CommandManager.literal;

/**
 * Test mod for in-game testing of the interaction system.
 * Provides commands to test attack, collect, defend, and communication functionality.
 */
public class InteractionTestMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        registerTestCommands();
    }
    
    private void registerTestCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("aimobs")
                .then(literal("test")
                    .then(literal("attack")
                        .executes(context -> {
                            testAttackCommand(context.getSource());
                            return 1;
                        }))
                    .then(literal("collect")
                        .executes(context -> {
                            testCollectCommand(context.getSource());
                            return 1;
                        }))
                    .then(literal("defend")
                        .executes(context -> {
                            testDefendCommand(context.getSource());
                            return 1;
                        }))
                    .then(literal("speak")
                        .executes(context -> {
                            testCommunicationCommand(context.getSource());
                            return 1;
                        }))
                    .then(literal("inventory")
                        .executes(context -> {
                            testInventorySystem(context.getSource());
                            return 1;
                        }))
                    .then(literal("spawn")
                        .executes(context -> {
                            spawnTestWolf(context.getSource());
                            return 1;
                        }))
                    .then(literal("status")
                        .executes(context -> {
                            showWolfStatus(context.getSource());
                            return 1;
                        }))));
        });
    }
    
    private void testAttackCommand(ServerCommandSource source) {
        try {
            AiControlledWolfEntity wolf = findNearestWolf(source);
            if (wolf == null) {
                source.sendFeedback(() -> Text.literal("No AI wolf found nearby. Use /aimobs test spawn first."), false);
                return;
            }
            
            // Spawn a zombie for testing
            ServerWorld world = source.getWorld();
            Vec3d wolfPos = wolf.getPos();
            ZombieEntity zombie = new ZombieEntity(EntityType.ZOMBIE, world);
            zombie.setPosition(wolfPos.x + 5, wolfPos.y, wolfPos.z);
            world.spawnEntity(zombie);
            
            // Test attack command
            NetworkMessage attackMessage = createTestMessage("attack", Map.of("target", "zombie"));
            var attackCommand = wolf.createInteractionCommand(attackMessage);
            if (attackCommand != null) {
                wolf.executeCommand(attackCommand);
                source.sendFeedback(() -> Text.literal("Attack command sent! Wolf should attack the nearby zombie."), false);
            } else {
                source.sendFeedback(() -> Text.literal("Failed to create attack command."), false);
            }
            
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("Error testing attack: " + e.getMessage()), false);
        }
    }
    
    private void testCollectCommand(ServerCommandSource source) {
        try {
            AiControlledWolfEntity wolf = findNearestWolf(source);
            if (wolf == null) {
                source.sendFeedback(() -> Text.literal("No AI wolf found nearby. Use /aimobs test spawn first."), false);
                return;
            }
            
            // Drop some items for testing
            ServerWorld world = source.getWorld();
            Vec3d wolfPos = wolf.getPos();
            
            // Drop wood items
            for (int i = 0; i < 3; i++) {
                ItemStack stack = new ItemStack(Items.OAK_LOG, 1);
                wolf.dropStack(stack);
            }
            
            // Test collect command
            NetworkMessage collectMessage = createTestMessage("collect", Map.of(
                "item", "wood",
                "radius", 10.0,
                "max_items", 5
            ));
            var collectCommand = wolf.createInteractionCommand(collectMessage);
            if (collectCommand != null) {
                wolf.executeCommand(collectCommand);
                source.sendFeedback(() -> Text.literal("Collect command sent! Wolf should collect nearby wood items."), false);
            } else {
                source.sendFeedback(() -> Text.literal("Failed to create collect command."), false);
            }
            
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("Error testing collect: " + e.getMessage()), false);
        }
    }
    
    private void testDefendCommand(ServerCommandSource source) {
        try {
            AiControlledWolfEntity wolf = findNearestWolf(source);
            if (wolf == null) {
                source.sendFeedback(() -> Text.literal("No AI wolf found nearby. Use /aimobs test spawn first."), false);
                return;
            }
            
            // Test defend command
            NetworkMessage defendMessage = createTestMessage("defend", Map.of(
                "area", "here",
                "radius", 15.0
            ));
            var defendCommand = wolf.createInteractionCommand(defendMessage);
            if (defendCommand != null) {
                wolf.executeCommand(defendCommand);
                source.sendFeedback(() -> Text.literal("Defend command sent! Wolf should start patrolling this area."), false);
            } else {
                source.sendFeedback(() -> Text.literal("Failed to create defend command."), false);
            }
            
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("Error testing defend: " + e.getMessage()), false);
        }
    }
    
    private void testCommunicationCommand(ServerCommandSource source) {
        try {
            AiControlledWolfEntity wolf = findNearestWolf(source);
            if (wolf == null) {
                source.sendFeedback(() -> Text.literal("No AI wolf found nearby. Use /aimobs test spawn first."), false);
                return;
            }
            
            // Test communication command
            NetworkMessage speakMessage = createTestMessage("speak", Map.of(
                "message", "What do you see?"
            ));
            var speakCommand = wolf.createInteractionCommand(speakMessage);
            if (speakCommand != null) {
                wolf.executeCommand(speakCommand);
                
                // Get interaction service to process the communication
                InteractionService interactionService = wolf.getInteractionService();
                String response = interactionService.processCommunication("What do you see?");
                
                source.sendFeedback(() -> Text.literal("Wolf response: " + (response != null ? response : "No response")), false);
            } else {
                source.sendFeedback(() -> Text.literal("Failed to create communication command."), false);
            }
            
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("Error testing communication: " + e.getMessage()), false);
        }
    }
    
    private void testInventorySystem(ServerCommandSource source) {
        try {
            AiControlledWolfEntity wolf = findNearestWolf(source);
            if (wolf == null) {
                source.sendFeedback(() -> Text.literal("No AI wolf found nearby. Use /aimobs test spawn first."), false);
                return;
            }
            
            InventoryActions inventory = wolf.getInventoryActions();
            
            // Add some test items
            inventory.addItem(new ItemStack(Items.OAK_LOG, 5));
            inventory.addItem(new ItemStack(Items.STONE, 3));
            inventory.addItem(new ItemStack(Items.IRON_INGOT, 2));
            
            // Show inventory status
            List<ItemStack> items = inventory.getAllItems();
            source.sendFeedback(() -> Text.literal("Wolf inventory (" + inventory.getItemCount() + "/" + inventory.getMaxCapacity() + "):"), false);
            
            for (ItemStack stack : items) {
                String itemName = stack.getItem().toString();
                int count = stack.getCount();
                source.sendFeedback(() -> Text.literal("  - " + itemName + " x" + count), false);
            }
            
            // Test dropping an item
            boolean dropped = inventory.dropItem(new ItemStack(Items.OAK_LOG, 1));
            source.sendFeedback(() -> Text.literal("Dropped item: " + dropped), false);
            
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("Error testing inventory: " + e.getMessage()), false);
        }
    }
    
    private void spawnTestWolf(ServerCommandSource source) {
        try {
            ServerWorld world = source.getWorld();
            Vec3d pos = source.getPosition();
            
            AiControlledWolfEntity wolf = new AiControlledWolfEntity(EntityType.WOLF, world);
            wolf.setPosition(pos.x + 2, pos.y, pos.z);
            wolf.setCustomName(Text.literal("AI Test Wolf"));
            
            if (world.spawnEntity(wolf)) {
                source.sendFeedback(() -> Text.literal("Spawned AI-controlled wolf nearby!"), false);
            } else {
                source.sendFeedback(() -> Text.literal("Failed to spawn wolf."), false);
            }
            
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("Error spawning wolf: " + e.getMessage()), false);
        }
    }
    
    private void showWolfStatus(ServerCommandSource source) {
        try {
            AiControlledWolfEntity wolf = findNearestWolf(source);
            if (wolf == null) {
                source.sendFeedback(() -> Text.literal("No AI wolf found nearby."), false);
                return;
            }
            
            // Show wolf status
            var state = wolf.getCurrentState();
            var pos = wolf.getPos();
            var interactionService = wolf.getInteractionService();
            var inventoryActions = wolf.getInventoryActions();
            
            source.sendFeedback(() -> Text.literal("Wolf Status:"), false);
            source.sendFeedback(() -> Text.literal("  State: " + state.name()), false);
            source.sendFeedback(() -> Text.literal("  Position: (" + String.format("%.1f", pos.x) + ", " + String.format("%.1f", pos.y) + ", " + String.format("%.1f", pos.z) + ")"), false);
            source.sendFeedback(() -> Text.literal("  Interacting: " + interactionService.isInteracting()), false);
            source.sendFeedback(() -> Text.literal("  Inventory: " + inventoryActions.getItemCount() + "/" + inventoryActions.getMaxCapacity()), false);
            
            if (interactionService.getCurrentTargetPosition() != null) {
                Vec3d target = interactionService.getCurrentTargetPosition();
                source.sendFeedback(() -> Text.literal("  Target: (" + String.format("%.1f", target.x) + ", " + String.format("%.1f", target.y) + ", " + String.format("%.1f", target.z) + ")"), false);
            }
            
        } catch (Exception e) {
            source.sendFeedback(() -> Text.literal("Error getting wolf status: " + e.getMessage()), false);
        }
    }
    
    private AiControlledWolfEntity findNearestWolf(ServerCommandSource source) {
        ServerWorld world = source.getWorld();
        Vec3d pos = source.getPosition();
        
        List<AiControlledWolfEntity> wolves = world.getEntitiesByClass(
            AiControlledWolfEntity.class,
            new net.minecraft.util.math.Box(pos.add(-20, -10, -20), pos.add(20, 10, 20)),
            entity -> entity.isAlive()
        );
        
        return wolves.stream()
            .min((w1, w2) -> Double.compare(pos.distanceTo(w1.getPos()), pos.distanceTo(w2.getPos())))
            .orElse(null);
    }
    
    private NetworkMessage createTestMessage(String action, Map<String, Object> parameters) {
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