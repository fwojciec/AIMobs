package com.aimobs.entity.ai;

import com.aimobs.entity.TestCommand;
import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.entity.ai.core.AIState;
import com.aimobs.entity.ai.core.CommandExecutor;
import com.aimobs.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

class CommandExecutionTest extends BaseUnitTest {
    
    private MockCommandExecutor executor;
    
    @BeforeEach
    void setUp() {
        executor = new MockCommandExecutor();
    }
    
    @Test
    void shouldStartWithIdleState() {
        assertEquals(AIState.IDLE, executor.getCurrentState());
    }
    
    @Test
    void shouldHaveEmptyCommandQueueInitially() {
        assertTrue(executor.getCommandQueue().isEmpty());
    }
    
    @Test
    void shouldExecuteCommand() {
        TestCommand command = new TestCommand();
        executor.executeCommand(command);
        
        assertEquals(AIState.BUSY, executor.getCurrentState());
        assertFalse(executor.getCommandQueue().isEmpty());
    }
    
    @Test
    void shouldStopCurrentCommand() {
        TestCommand command = new TestCommand();
        executor.executeCommand(command);
        executor.tick(); // Start processing the command
        executor.stopCurrentCommand();
        
        assertEquals(AIState.IDLE, executor.getCurrentState());
        assertTrue(command.isCancelled());
    }
    
    @Test
    void shouldProcessCommandQueue() {
        TestCommand command1 = new TestCommand();
        TestCommand command2 = new TestCommand();
        
        executor.executeCommand(command1);
        executor.executeCommand(command2);
        
        assertEquals(2, executor.getCommandQueue().size());
        
        executor.tick();
        assertTrue(command1.isComplete());
        assertEquals(1, executor.getCommandQueue().size());
        
        executor.tick();
        assertTrue(command2.isComplete());
        assertTrue(executor.getCommandQueue().isEmpty());
        
        executor.tick(); // Process command2 completion
        assertEquals(AIState.IDLE, executor.getCurrentState());
    }
    
    private static class MockCommandExecutor implements CommandExecutor {
        private AIState currentState = AIState.IDLE;
        private final Queue<AICommand> commandQueue = new LinkedList<>();
        private AICommand currentCommand;
        
        @Override
        public void executeCommand(AICommand command) {
            if (command == null) return;
            
            this.commandQueue.offer(command);
            this.currentState = AIState.BUSY;
        }
        
        @Override
        public void stopCurrentCommand() {
            if (this.currentCommand != null) {
                this.currentCommand.cancel();
                this.currentCommand = null;
            }
            this.commandQueue.clear();
            this.currentState = AIState.IDLE;
        }
        
        @Override
        public AIState getCurrentState() {
            return this.currentState;
        }
        
        @Override
        public Queue<AICommand> getCommandQueue() {
            return new LinkedList<>(this.commandQueue);
        }
        
        public void tick() {
            if (this.currentCommand == null && !this.commandQueue.isEmpty()) {
                processNextCommand();
            } else if (this.currentCommand != null && this.currentCommand.isComplete()) {
                this.currentCommand = null;
                processNextCommand();
            }
        }
        
        private void processNextCommand() {
            if (!this.commandQueue.isEmpty()) {
                this.currentCommand = this.commandQueue.poll();
                this.currentState = AIState.BUSY;
                this.currentCommand.execute();
            } else {
                this.currentCommand = null;
                this.currentState = AIState.IDLE;
            }
        }
    }
}