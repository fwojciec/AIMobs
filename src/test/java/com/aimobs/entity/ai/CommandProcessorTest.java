package com.aimobs.entity.ai;

import com.aimobs.entity.TestCommand;
import com.aimobs.entity.ai.CommandProcessorService;
import com.aimobs.entity.ai.ServiceFactory;
import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.entity.ai.core.AIState;
import com.aimobs.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CommandProcessor following Kent Beck's TDD principles.
 * Each test focuses on a single behavior and uses clear, readable assertions.
 */
class CommandProcessorTest extends BaseUnitTest {
    
    private CommandProcessorService processor;
    private Queue<AICommand> commandQueue;
    
    @BeforeEach
    void setUp() {
        commandQueue = new LinkedList<>();
        processor = ServiceFactory.createCommandProcessor(commandQueue);
    }
    
    @Test
    void shouldStartWithIdleState() {
        assertEquals(AIState.IDLE, processor.getCurrentState());
    }
    
    @Test
    void shouldHaveEmptyCommandQueueInitially() {
        assertTrue(processor.getCommandQueue().isEmpty());
    }
    
    @Test
    void shouldAcceptAndQueueCommands() {
        TestCommand command = new TestCommand();
        
        processor.executeCommand(command);
        
        assertEquals(AIState.BUSY, processor.getCurrentState());
        assertEquals(1, commandQueue.size());
        assertSame(command, commandQueue.peek());
    }
    
    @Test
    void shouldIgnoreNullCommands() {
        processor.executeCommand(null);
        
        assertEquals(AIState.IDLE, processor.getCurrentState());
        assertTrue(commandQueue.isEmpty());
    }
    
    @Test
    void shouldProcessCommandsOnTick() {
        TestCommand command = new TestCommand();
        processor.executeCommand(command);
        
        boolean stateChanged = processor.tick();
        
        assertTrue(stateChanged);
        assertTrue(command.isComplete());
        assertSame(command, processor.getCurrentCommand());
    }
    
    @Test
    void shouldTransitionToIdleAfterCommandCompletion() {
        TestCommand command = new TestCommand();
        processor.executeCommand(command);
        processor.tick(); // Start command
        
        boolean stateChanged = processor.tick(); // Complete command
        
        assertTrue(stateChanged);
        assertEquals(AIState.IDLE, processor.getCurrentState());
        assertNull(processor.getCurrentCommand());
    }
    
    @Test
    void shouldProcessCommandQueueInOrder() {
        TestCommand command1 = new TestCommand();
        TestCommand command2 = new TestCommand();
        processor.executeCommand(command1);
        processor.executeCommand(command2);
        
        processor.tick(); // Process command1
        assertTrue(command1.isComplete());
        assertEquals(1, commandQueue.size());
        
        processor.tick(); // Complete command1, start command2
        processor.tick(); // Complete command2
        
        assertTrue(command2.isComplete());
        assertEquals(AIState.IDLE, processor.getCurrentState());
        assertTrue(commandQueue.isEmpty());
    }
    
    @Test
    void shouldStopCurrentCommandAndClearQueue() {
        TestCommand command1 = new TestCommand();
        TestCommand command2 = new TestCommand();
        processor.executeCommand(command1);
        processor.executeCommand(command2);
        processor.tick(); // Start command1
        
        processor.stopCurrentCommand();
        
        assertEquals(AIState.IDLE, processor.getCurrentState());
        assertTrue(command1.isCancelled());
        assertTrue(commandQueue.isEmpty());
        assertNull(processor.getCurrentCommand());
    }
    
    @Test
    void shouldReturnFalseWhenNoStateChange() {
        // Create a command that doesn't auto-complete
        AICommand longRunningCommand = new AICommand() {
            private boolean executed = false;
            
            @Override
            public void execute() {
                executed = true;
            }
            
            @Override
            public boolean isComplete() {
                return false; // Never completes for this test
            }
            
            @Override
            public void cancel() {
                // No-op for this test
            }
        };
        
        processor.executeCommand(longRunningCommand);
        processor.tick(); // Start command
        
        // Tick again while command is still running
        boolean stateChanged = processor.tick();
        
        assertFalse(stateChanged);
    }
}