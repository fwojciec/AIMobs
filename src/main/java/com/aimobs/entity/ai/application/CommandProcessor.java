package com.aimobs.entity.ai.application;

import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.entity.ai.core.AIState;
import com.aimobs.entity.ai.FeedbackService;

import java.util.Queue;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.aimobs.entity.ai.CommandProcessorService;

/**
 * Application layer implementation of CommandProcessorService.
 * Separates command processing logic from entity implementation.
 * Follows Single Responsibility Principle and enables easy testing.
 */
public class CommandProcessor implements CommandProcessorService {
    
    private static final Logger LOGGER = Logger.getLogger(CommandProcessor.class.getName());
    
    private AIState currentState = AIState.IDLE;
    private final Queue<AICommand> commandQueue;
    private AICommand currentCommand;
    private final FeedbackService feedbackService;
    private final UUID wolfId;
    
    public CommandProcessor(Queue<AICommand> commandQueue) {
        this(commandQueue, null, null);
    }
    
    public CommandProcessor(Queue<AICommand> commandQueue, FeedbackService feedbackService, UUID wolfId) {
        this.commandQueue = commandQueue;
        this.feedbackService = feedbackService;
        this.wolfId = wolfId;
        LOGGER.log(Level.INFO, "CommandProcessor created with queue: " + commandQueue.getClass().getSimpleName());
    }
    
    public void executeCommand(AICommand command) {
        if (command == null) {
            LOGGER.log(Level.WARNING, "Attempted to execute null command");
            return;
        }
        
        LOGGER.log(Level.INFO, "Queueing command: " + command.getClass().getSimpleName());
        
        // Trigger feedback for command received
        if (feedbackService != null && wolfId != null) {
            feedbackService.onCommandReceived(wolfId, command);
        }
        
        this.commandQueue.offer(command);
        this.currentState = AIState.BUSY;
        LOGGER.log(Level.INFO, "Command queue size after adding: " + this.commandQueue.size());
    }
    
    public void stopCurrentCommand() {
        LOGGER.log(Level.INFO, "Stopping current command. Current command: " + 
                  (this.currentCommand != null ? this.currentCommand.getClass().getSimpleName() : "null"));
        if (this.currentCommand != null) {
            this.currentCommand.cancel();
            this.currentCommand = null;
        }
        this.commandQueue.clear();
        this.currentState = AIState.IDLE;
        LOGGER.log(Level.INFO, "Command processing stopped, state set to IDLE");
    }
    
    public AIState getCurrentState() {
        return this.currentState;
    }
    
    public Queue<AICommand> getCommandQueue() {
        return this.commandQueue;
    }
    
    /**
     * Process pending commands. Call this from entity tick() method.
     * @return true if state changed, false otherwise
     */
    public boolean tick() {
        LOGGER.log(Level.FINE, "CommandProcessor tick() called. Queue size: " + this.commandQueue.size() + 
                  ", Current command: " + (this.currentCommand != null ? this.currentCommand.getClass().getSimpleName() : "null") +
                  ", Current state: " + this.currentState);
        
        boolean stateChanged = false;
        
        if (this.currentCommand == null && !this.commandQueue.isEmpty()) {
            LOGGER.log(Level.INFO, "No current command and queue not empty - processing next command");
            processNextCommand();
            stateChanged = true;
        } else if (this.currentCommand != null && this.currentCommand.isComplete()) {
            LOGGER.log(Level.INFO, "Current command completed: " + this.currentCommand.getClass().getSimpleName());
            this.currentCommand = null;
            processNextCommand();
            stateChanged = true;
        } else if (this.currentCommand == null && this.commandQueue.isEmpty()) {
            LOGGER.log(Level.FINE, "No commands to process - queue is empty");
        } else if (this.currentCommand != null && !this.currentCommand.isComplete()) {
            LOGGER.log(Level.FINE, "Current command still executing: " + this.currentCommand.getClass().getSimpleName());
        }
        
        return stateChanged;
    }
    
    private void processNextCommand() {
        if (!this.commandQueue.isEmpty()) {
            this.currentCommand = this.commandQueue.poll();
            LOGGER.log(Level.INFO, "Starting execution of command: " + this.currentCommand.getClass().getSimpleName());
            this.currentState = AIState.BUSY;
            try {
                this.currentCommand.execute();
                LOGGER.log(Level.INFO, "Command execute() method called successfully for: " + this.currentCommand.getClass().getSimpleName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception during command execution: " + this.currentCommand.getClass().getSimpleName(), e);
            }
        } else {
            LOGGER.log(Level.INFO, "No commands in queue - setting state to IDLE");
            this.currentCommand = null;
            this.currentState = AIState.IDLE;
        }
    }
    
    public AICommand getCurrentCommand() {
        return this.currentCommand;
    }
}