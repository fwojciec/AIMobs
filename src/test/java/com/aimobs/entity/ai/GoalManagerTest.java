package com.aimobs.entity.ai;

import com.aimobs.entity.ai.GoalService;
import com.aimobs.entity.ai.ServiceFactory;
import com.aimobs.entity.ai.core.EntityActions;
import com.aimobs.test.BaseUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * Tests for GoalService using Justin Searls' approach - focusing on behavior verification.
 * Tests the service's orchestration of goal setup without coupling to implementation details.
 */
class GoalCoordinatorTest extends BaseUnitTest {
    
    @Mock
    private EntityActions mockEntityActions;
    
    private GoalService goalService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        goalService = ServiceFactory.createGoalService(mockEntityActions);
    }
    
    @Test
    void shouldInitializeAIGoalsInCorrectOrder() {
        goalService.initializeAIGoals();
        
        // Verify the sequence of goal setup operations
        var inOrder = inOrder(mockEntityActions);
        inOrder.verify(mockEntityActions).clearGoals();
        inOrder.verify(mockEntityActions).addSwimGoal();
        inOrder.verify(mockEntityActions).addEscapeDangerGoal();
        inOrder.verify(mockEntityActions).addControllableGoal();
    }
    
    @Test
    void shouldCallAllGoalSetupMethods() {
        goalService.initializeAIGoals();
        
        verify(mockEntityActions).clearGoals();
        verify(mockEntityActions).addSwimGoal();
        verify(mockEntityActions).addEscapeDangerGoal();
        verify(mockEntityActions).addControllableGoal();
        verifyNoMoreInteractions(mockEntityActions);
    }
}