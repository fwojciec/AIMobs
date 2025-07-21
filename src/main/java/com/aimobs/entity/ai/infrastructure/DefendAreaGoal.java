package com.aimobs.entity.ai.infrastructure;

import com.aimobs.entity.ai.InteractionService;
import com.aimobs.entity.ai.core.DefendAreaCommand;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 * Minecraft AI Goal for defending a specific area from hostile entities.
 * Infrastructure layer - platform-specific implementation.
 * 
 * Following Ben Johnson's standard package layout:
 * - Infrastructure layer adapts external systems to internal interfaces
 * - Can depend on all other layers
 */
public class DefendAreaGoal extends Goal {
    
    private final WolfEntity wolf;
    private final InteractionService interactionService;
    private final DefendAreaCommand command;
    private final Random random = new Random();
    
    private LivingEntity currentThreat;
    private Vec3d patrolTarget;
    private int patrolCooldown = 0;
    private int threatScanCooldown = 0;
    
    private static final double THREAT_DETECTION_RANGE = 16.0;
    private static final double ATTACK_RANGE = 2.0;
    private static final int PATROL_COOLDOWN_TICKS = 100; // ~5 seconds
    private static final int THREAT_SCAN_INTERVAL = 20; // ~1 second
    
    public DefendAreaGoal(WolfEntity wolf, InteractionService interactionService, DefendAreaCommand command) {
        this.wolf = wolf;
        this.interactionService = interactionService;
        this.command = command;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.TARGET));
    }
    
    @Override
    public boolean canStart() {
        return !command.isComplete() && !command.isCancelled();
    }
    
    @Override
    public boolean shouldContinue() {
        return !command.isComplete() && !command.isCancelled();
    }
    
    @Override
    public void start() {
        // Start defending - set initial patrol target
        generatePatrolTarget();
    }
    
    @Override
    public void tick() {
        if (command.isComplete() || command.isCancelled()) {
            return;
        }
        
        // Scan for threats periodically
        if (--threatScanCooldown <= 0) {
            scanForThreats();
            threatScanCooldown = THREAT_SCAN_INTERVAL;
        }
        
        if (currentThreat != null && currentThreat.isAlive()) {
            handleThreat();
        } else {
            patrol();
        }
        
        // Update interaction progress
        interactionService.updateInteractionProgress();
    }
    
    @Override
    public void stop() {
        wolf.setTarget(null);
        wolf.getNavigation().stop();
        currentThreat = null;
        patrolTarget = null;
    }
    
    @Override
    public boolean canStop() {
        // Allow higher priority goals to interrupt
        return true;
    }
    
    private void scanForThreats() {
        BlockPos center = command.getCenterPos();
        double radius = Math.max(command.getRadius(), THREAT_DETECTION_RANGE);
        
        Box searchBox = new Box(
            center.getX() - radius, center.getY() - radius, center.getZ() - radius,
            center.getX() + radius, center.getY() + radius, center.getZ() + radius
        );
        
        List<HostileEntity> threats = wolf.getWorld()
            .getEntitiesByClass(HostileEntity.class, searchBox, this::isValidThreat);
        
        if (!threats.isEmpty()) {
            // Find closest threat within defense area
            currentThreat = threats.stream()
                .filter(this::isThreatInDefenseArea)
                .min((a, b) -> Double.compare(wolf.distanceTo(a), wolf.distanceTo(b)))
                .orElse(null);
        } else {
            currentThreat = null;
        }
    }
    
    private boolean isValidThreat(LivingEntity entity) {
        return entity != null && entity.isAlive() && 
               entity instanceof HostileEntity &&
               wolf.distanceTo(entity) <= THREAT_DETECTION_RANGE;
    }
    
    private boolean isThreatInDefenseArea(LivingEntity threat) {
        BlockPos center = command.getCenterPos();
        double distance = Math.sqrt(threat.getBlockPos().getSquaredDistance(center));
        return distance <= command.getRadius();
    }
    
    private void handleThreat() {
        if (currentThreat == null || !currentThreat.isAlive()) {
            currentThreat = null;
            return;
        }
        
        // Check if threat is still in defense area
        if (!isThreatInDefenseArea(currentThreat)) {
            currentThreat = null;
            return;
        }
        
        wolf.setTarget(currentThreat);
        double distance = wolf.distanceTo(currentThreat);
        
        if (distance <= ATTACK_RANGE) {
            // Attack the threat
            wolf.tryAttack(currentThreat);
        } else {
            // Move towards the threat
            wolf.getNavigation().startMovingTo(currentThreat, 1.2);
            
            // Request positioning assistance
            Vec3d threatPos = currentThreat.getPos();
            interactionService.coordinatePositioning(threatPos);
        }
    }
    
    private void patrol() {
        if (--patrolCooldown <= 0 || patrolTarget == null || 
            wolf.getNavigation().isIdle() || wolf.getPos().distanceTo(patrolTarget) < 2.0) {
            generatePatrolTarget();
            patrolCooldown = PATROL_COOLDOWN_TICKS;
        }
        
        if (patrolTarget != null) {
            wolf.getNavigation().startMovingTo(patrolTarget.x, patrolTarget.y, patrolTarget.z, 0.8);
        }
    }
    
    private void generatePatrolTarget() {
        BlockPos center = command.getCenterPos();
        double radius = command.getRadius() * 0.8; // Patrol within 80% of defense radius
        
        double angle = random.nextDouble() * 2 * Math.PI;
        double distance = random.nextDouble() * radius;
        
        double x = center.getX() + Math.cos(angle) * distance;
        double z = center.getZ() + Math.sin(angle) * distance;
        
        // Find suitable Y coordinate
        BlockPos targetPos = new BlockPos((int)x, center.getY(), (int)z);
        BlockPos groundPos = wolf.getWorld().getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, targetPos);
        
        patrolTarget = new Vec3d(x, groundPos.getY(), z);
    }
    
    /**
     * @return The defend command this goal is executing
     */
    public DefendAreaCommand getCommand() {
        return command;
    }
    
    /**
     * @return The current threat being handled, or null if none
     */
    public LivingEntity getCurrentThreat() {
        return currentThreat;
    }
    
    /**
     * @return The current patrol target position, or null if none
     */
    public Vec3d getPatrolTarget() {
        return patrolTarget;
    }
}