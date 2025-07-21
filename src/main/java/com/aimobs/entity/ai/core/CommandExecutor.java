package com.aimobs.entity.ai.core;

import java.util.Queue;

public interface CommandExecutor {
    void executeCommand(AICommand command);
    void stopCurrentCommand();
    AIState getCurrentState();
    Queue<AICommand> getCommandQueue();
}