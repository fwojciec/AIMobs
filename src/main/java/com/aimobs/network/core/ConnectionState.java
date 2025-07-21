package com.aimobs.network.core;

/**
 * Core domain enum representing WebSocket connection states.
 * Following Standard Package Layout - this is a domain primitive.
 */
public enum ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR,
    SHUTDOWN
}