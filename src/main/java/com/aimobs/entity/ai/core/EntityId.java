package com.aimobs.entity.ai.core;

import java.util.UUID;
import java.util.Objects;

/**
 * Value object representing a unique entity identifier.
 * Used to identify AI-controlled entities across save/load cycles.
 * 
 * Core domain object - no dependencies, pure data.
 */
public final class EntityId {
    private final UUID uuid;

    public EntityId(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid, "UUID cannot be null");
    }

    public static EntityId generate() {
        return new EntityId(UUID.randomUUID());
    }

    public static EntityId fromString(String uuidString) {
        return new EntityId(UUID.fromString(uuidString));
    }

    public UUID getUuid() {
        return uuid;
    }

    public String asString() {
        return uuid.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EntityId entityId = (EntityId) obj;
        return Objects.equals(uuid, entityId.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "EntityId{" + uuid + "}";
    }
}