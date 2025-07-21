package com.aimobs.entity.ai.core;

import com.aimobs.entity.ai.MovementService;
import com.aimobs.entity.ai.application.MovementCoordinator;
import com.aimobs.test.BaseUnitTest;
import com.aimobs.test.FakeEntityActions;
import com.aimobs.test.FakePathfindingService;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoveToLocationCommandIntegrationTest extends BaseUnitTest {

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
    void shouldCompleteWhenMovementSucceeds() {
        BlockPos target = new BlockPos(5, 64, 5);
        MovementTarget movementTarget = new MovementTarget(target);
        fakePathfindingService.setCanReachTarget(true);
        
        MoveToLocationCommand command = new MoveToLocationCommand(movementService, movementTarget);

        // Command should not be complete initially
        assertThat(command.isComplete()).isFalse();

        // Execute command
        command.execute();
        
        // Movement should have started
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.MOVING_TO_LOCATION);
        assertThat(command.isComplete()).isFalse();

        // Simulate reaching the target
        fakeEntityActions.setPosition(new Vec3d(5.0, 64.0, 5.0));
        fakePathfindingService.setHasReachedTarget(true);
        fakePathfindingService.setMoving(false);
        movementService.updateMovementProgress();

        // Command should now be complete
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.IDLE);
        assertThat(command.isComplete()).isTrue();
    }

    @Test
    void shouldCompleteWhenPathfindingFails() {
        BlockPos target = new BlockPos(5, 64, 5);
        MovementTarget movementTarget = new MovementTarget(target);
        fakePathfindingService.setCanReachTarget(false); // Target unreachable
        
        MoveToLocationCommand command = new MoveToLocationCommand(movementService, movementTarget);

        // Execute command
        command.execute();
        
        // Movement should fail immediately due to unreachable target
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.PATHFINDING_FAILED);
        assertThat(command.isComplete()).isTrue();
    }

    @Test
    void shouldCompleteWhenPathfindingFailsDuringMovement() {
        BlockPos target = new BlockPos(5, 64, 5);
        MovementTarget movementTarget = new MovementTarget(target);
        fakePathfindingService.setCanReachTarget(true);
        
        MoveToLocationCommand command = new MoveToLocationCommand(movementService, movementTarget);

        // Execute command
        command.execute();
        
        // Movement should have started
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.MOVING_TO_LOCATION);
        assertThat(command.isComplete()).isFalse();

        // Simulate pathfinding failure during movement
        fakePathfindingService.setHasPathfindingFailed(true);
        movementService.updateMovementProgress();

        // Command should now be complete due to failure
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.PATHFINDING_FAILED);
        assertThat(command.isComplete()).isTrue();
    }

    @Test
    void shouldCompleteWhenCancelled() {
        BlockPos target = new BlockPos(5, 64, 5);
        MovementTarget movementTarget = new MovementTarget(target);
        fakePathfindingService.setCanReachTarget(true);
        
        MoveToLocationCommand command = new MoveToLocationCommand(movementService, movementTarget);

        // Execute command
        command.execute();
        assertThat(command.isComplete()).isFalse();

        // Cancel command
        command.cancel();

        // Command should be complete and movement stopped
        assertThat(command.isComplete()).isTrue();
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.IDLE);
    }

    @Test
    void shouldNotExecuteWhenCancelled() {
        BlockPos target = new BlockPos(5, 64, 5);
        MovementTarget movementTarget = new MovementTarget(target);
        
        MoveToLocationCommand command = new MoveToLocationCommand(movementService, movementTarget);

        // Cancel before execution
        command.cancel();
        command.execute();

        // Movement should not have started
        assertThat(movementService.getCurrentState()).isEqualTo(MovementState.IDLE);
        assertThat(command.isComplete()).isTrue();
    }
}