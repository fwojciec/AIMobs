package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.AICommand;
import com.aimobs.entity.ai.core.AIState;

import java.util.Queue;

/**
 * Root package interface - defines command processing contract.
 * No dependencies on subpackages.
 * 
 * Following standard package layout:
 * - Root package contains interfaces
 * - Subpackages provide implementations
 * - Wiring happens at composition root
 */
public interface CommandProcessorService {
    void executeCommand(AICommand command);
    void stopCurrentCommand();
    AIState getCurrentState();
    Queue<AICommand> getCommandQueue();
    boolean tick();
    AICommand getCurrentCommand();
}