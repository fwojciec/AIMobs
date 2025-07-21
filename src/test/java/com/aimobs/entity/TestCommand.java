package com.aimobs.entity;

import com.aimobs.entity.ai.core.AICommand;

public class TestCommand implements AICommand {
    private boolean complete = false;
    private boolean cancelled = false;
    
    @Override
    public void execute() {
        // Simulate command execution
        complete = true;
    }
    
    @Override
    public boolean isComplete() {
        return complete;
    }
    
    @Override
    public void cancel() {
        cancelled = true;
        complete = true;
    }
    
    public boolean isCancelled() {
        return cancelled;
    }
    
    public void reset() {
        complete = false;
        cancelled = false;
    }
}