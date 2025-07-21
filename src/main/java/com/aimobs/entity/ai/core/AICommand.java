package com.aimobs.entity.ai.core;

public interface AICommand {
    void execute();
    boolean isComplete();
    void cancel();
}