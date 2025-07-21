package com.aimobs.entity.ai.core;

import com.aimobs.test.BaseUnitTest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovementTargetTest extends BaseUnitTest {

    @Test
    void shouldCreateTargetFromBlockPos() {
        BlockPos blockPos = new BlockPos(10, 64, 10);
        
        MovementTarget target = new MovementTarget(blockPos);
        
        assertThat(target.getBlockPos()).isEqualTo(blockPos);
        assertThat(target.getPosition()).isEqualTo(new Vec3d(10.5, 64.0, 10.5)); // Center of block
    }

    @Test
    void shouldCreateTargetFromVec3d() {
        Vec3d position = new Vec3d(10.5, 64.0, 10.5);
        
        MovementTarget target = new MovementTarget(position);
        
        assertThat(target.getPosition()).isEqualTo(position);
        assertThat(target.getBlockPos()).isEqualTo(new BlockPos(10, 64, 10));
    }

    @Test
    void shouldRejectNullBlockPos() {
        assertThatThrownBy(() -> new MovementTarget((BlockPos) null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullVec3d() {
        assertThatThrownBy(() -> new MovementTarget((Vec3d) null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldCalculateDistanceFromPosition() {
        MovementTarget target = new MovementTarget(new BlockPos(10, 64, 10));
        Vec3d origin = new Vec3d(0, 64, 0);
        
        double distance = target.distanceFrom(origin);
        
        // Distance from (0,64,0) to (10.5,64,10.5) = sqrt(10.5^2 + 0^2 + 10.5^2) = sqrt(220.5) ≈ 14.85
        assertThat(distance).isCloseTo(14.85, org.assertj.core.data.Offset.offset(0.1));
    }

    @Test
    void shouldBeImmutable() {
        BlockPos originalPos = new BlockPos(10, 64, 10);
        MovementTarget target = new MovementTarget(originalPos);
        
        // Modifying original shouldn't affect target
        BlockPos modifiedPos = originalPos.up();
        
        assertThat(target.getBlockPos()).isEqualTo(new BlockPos(10, 64, 10));
        assertThat(target.getBlockPos()).isNotEqualTo(modifiedPos);
    }

    @Test
    void shouldImplementEqualsAndHashCode() {
        MovementTarget target1 = new MovementTarget(new BlockPos(10, 64, 10));
        MovementTarget target2 = new MovementTarget(new BlockPos(10, 64, 10));
        MovementTarget target3 = new MovementTarget(new BlockPos(11, 64, 10));
        
        assertThat(target1).isEqualTo(target2);
        assertThat(target1).isNotEqualTo(target3);
        assertThat(target1.hashCode()).isEqualTo(target2.hashCode());
    }

    @Test
    void shouldProvideStringRepresentation() {
        MovementTarget target = new MovementTarget(new BlockPos(10, 64, 10));
        
        assertThat(target.toString()).contains("10", "64", "10");
    }
}