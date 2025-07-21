package com.aimobs.entity.ai;

import com.aimobs.entity.ai.core.MovementTarget;
import com.aimobs.entity.ai.core.MovementState;
import com.aimobs.test.BaseUnitTest;
import com.aimobs.test.FakeEntityActions;
import com.aimobs.test.FakePathfindingService;
import com.aimobs.entity.ai.application.MovementCoordinator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MovementServiceTest extends BaseUnitTest {

    private MovementService movementService;
    private FakeEntityActions fakeEntityActions;
    private FakePathfindingService fakePathfindingService;

    @BeforeEach
    void setUp() {        
        fakeEntityActions = new FakeEntityActions();
        fakeEntityActions.setPosition(new Vec3d(0, 64, 0));
        
        fakePathfindingService = new FakePathfindingService();
        
        movementService = new MovementCoordinator(fakeEntityActions, fakePathfindingService);
    }

    @Test
    void shouldInitializeWithIdleState() {
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.IDLE);
    }

    @Test
    void shouldMoveToValidTarget() {
        BlockPos target = new BlockPos(5, 64, 5);
        MovementTarget movementTarget = new MovementTarget(target);
        fakePathfindingService.setCanReachTarget(true);

        movementService.moveTo(movementTarget);

        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.MOVING_TO_LOCATION);
        assertThat(movementService.getCurrentTarget()).isEqualTo(movementTarget);
    }

    @Test
    void shouldStopMovement() {
        // Setup wolf in moving state
        movementService.moveTo(new MovementTarget(new BlockPos(5, 64, 5)));
        
        movementService.stop();

        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.IDLE);
        assertThat(movementService.getCurrentTarget()).isNull();
    }

    @Test
    void shouldRejectNullTargets() {
        movementService.moveTo(null);

        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.IDLE);
    }

    @Test
    void shouldFailWhenTargetUnreachable() {
        BlockPos target = new BlockPos(5, 64, 5);
        MovementTarget movementTarget = new MovementTarget(target);
        fakePathfindingService.setCanReachTarget(false);

        movementService.moveTo(movementTarget);

        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.PATHFINDING_FAILED);
        assertThat(movementService.getCurrentTarget()).isNull();
    }

    @Test
    void shouldTransitionToFailedWhenPathfindingFails() {
        BlockPos target = new BlockPos(5, 64, 5);
        MovementTarget movementTarget = new MovementTarget(target);
        fakePathfindingService.setCanReachTarget(true);

        movementService.moveTo(movementTarget);
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.MOVING_TO_LOCATION);

        // Simulate pathfinding failure during movement
        fakePathfindingService.setHasPathfindingFailed(true);
        movementService.updateMovementProgress();

        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.PATHFINDING_FAILED);
        assertThat(movementService.getCurrentTarget()).isNull();
    }

    @Test
    void shouldCompleteMovementWhenReachingTarget() {
        BlockPos target = new BlockPos(5, 64, 5);
        MovementTarget movementTarget = new MovementTarget(target);
        fakePathfindingService.setCanReachTarget(true);

        movementService.moveTo(movementTarget);
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.MOVING_TO_LOCATION);

        // Simulate reaching the target
        fakeEntityActions.setPosition(new Vec3d(5.0, 64.0, 5.0)); // Close to target
        fakePathfindingService.setHasReachedTarget(true);
        fakePathfindingService.setMoving(false);
        movementService.updateMovementProgress();

        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.IDLE);
        assertThat(movementService.getCurrentTarget()).isNull();
    }

    @Test
    void shouldFailWhenStoppedMovingWithoutReachingTarget() {
        BlockPos target = new BlockPos(50, 64, 50); // Far target
        MovementTarget movementTarget = new MovementTarget(target);
        fakePathfindingService.setCanReachTarget(true);

        movementService.moveTo(movementTarget);
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.MOVING_TO_LOCATION);

        // Simulate stopping movement without reaching target (pathfinding gave up)
        fakePathfindingService.setMoving(false);
        fakePathfindingService.setHasReachedTarget(false);
        movementService.updateMovementProgress();

        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.PATHFINDING_FAILED);
        assertThat(movementService.getCurrentTarget()).isNull();
    }
}