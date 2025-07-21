package com.aimobs.entity.ai.core;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

/**
 * Immutable value object representing a movement destination.
 * Provides both block-based and precise positioning.
 */
public final class MovementTarget {
    private final BlockPos blockPos;
    private final Vec3d position;

    /**
     * Creates a movement target from a block position.
     * The precise position will be the center of the block.
     */
    public MovementTarget(BlockPos blockPos) {
        this.blockPos = Objects.requireNonNull(blockPos, "blockPos cannot be null");
        this.position = new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
    }

    /**
     * Creates a movement target from a precise position.
     * The block position will be derived from the coordinates.
     */
    public MovementTarget(Vec3d position) {
        this.position = Objects.requireNonNull(position, "position cannot be null");
        this.blockPos = new BlockPos((int) Math.floor(position.x), (int) Math.floor(position.y), (int) Math.floor(position.z));
    }

    /**
     * @return The block position of this target
     */
    public BlockPos getBlockPos() {
        return blockPos;
    }

    /**
     * @return The precise position of this target
     */
    public Vec3d getPosition() {
        return position;
    }

    /**
     * Calculates the distance from the given position to this target.
     * 
     * @param from The origin position
     * @return The distance in blocks
     */
    public double distanceFrom(Vec3d from) {
        return from.distanceTo(this.position);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MovementTarget that = (MovementTarget) obj;
        return Objects.equals(blockPos, that.blockPos) && Objects.equals(position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockPos, position);
    }

    @Override
    public String toString() {
        return "MovementTarget{" +
                "blockPos=" + blockPos +
                ", position=" + position +
                '}';
    }
}