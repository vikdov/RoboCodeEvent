package champions;

import robocode.*;
import java.awt.Color;
import java.util.*;

/**
 * PhantomAssassin - Elite Tournament-Grade Robocode Robot
 * Features: Predictive circular targeting, adaptive movement, energy metering,
 * multi-bot melee strategy, and advanced radar tracking
 */
public class PhantomAssassin extends AdvancedRobot {
    
    // Target tracking
    private static class Enemy {
        String name;
        double x, y, vx, vy, energy, heading;
        long lastScan;
        double bearingOffset;
        
        Enemy(String n) { name = n; }
    }
    
    private Map<String, Enemy> enemies = new HashMap<>();
    private String primaryTarget = "";
    private double radarAngle = 0;
    private int moveDirection = 1;
    private long lastMovementChange = 0;
    private static final double OPTIMAL_FIRE_POWER = 2.2;
    
    public void run() {
        // Colors for dominance
        setColors(Color.red, Color.black, Color.darkGray);
        
        // Advanced settings
        setAdjustRadarForRobotTurn(true);
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        
        // Main loop
        while (true) {
            updateTargetSelection();
            performRadarScan();
            execute();
        }
    }
    
    /**
     * Intelligent radar scanning - continuous 360° sweep with lock capability
     */
    private void performRadarScan() {
        if (!enemies.isEmpty()) {
            Enemy target = enemies.get(primaryTarget);
            if (target != null && getTime() - target.lastScan < 10) {
                // Lock onto primary target
                double radarTurn = normalizeBearing(
                    getHeading() + target.bearingOffset - getRadarHeading()
                );
                setTurnRadarRight(radarTurn);
            } else {
                // Sweep scan for new targets
                setTurnRadarRight(45);
            }
        } else {
            setTurnRadarRight(360);
        }
    }
    
    /**
     * Select the best target based on threat assessment
     */
    private void updateTargetSelection() {
        if (enemies.isEmpty()) return;
        
        double bestScore = Double.NEGATIVE_INFINITY;
        String bestTarget = "";
        
        for (Enemy e : enemies.values()) {
            if (getTime() - e.lastScan > 50) continue; // Stale data
            
            double distance = Math.hypot(getX() - e.x, getY() - e.y);
            double score = 0;
            
            // Threat assessment algorithm
            int botsAlive = enemies.size() + 1;
            
            if (botsAlive > 5) {
                // Early game: target closest (lower risk)
                score = 1000 - distance;
            } else if (botsAlive > 2) {
                // Mid game: target weakest threat
                score = (e.energy < 30 ? 500 : 0) + 
                        (1000 - distance) * 0.5 +
                        (100 - e.energy) * 2;
            } else {
                // Late game: target if closer or weaker
                score = (100 - e.energy) * 5 + (1000 - distance);
            }
            
            if (score > bestScore) {
                bestScore = score;
                bestTarget = e.name;
            }
        }
        
        primaryTarget = bestTarget;
    }
    
    /**
     * Onboard radar detection - primary method
     */
    public void onScannedRobot(ScannedRobotEvent e) {
        Enemy enemy = enemies.computeIfAbsent(e.getName(), Enemy::new);
        enemy.lastScan = getTime();
        
        // Update enemy position
        double absBearing = getHeading() + e.getBearing();
        enemy.bearingOffset = e.getBearing();
        enemy.x = getX() + e.getDistance() * Math.sin(Math.toRadians(absBearing));
        enemy.y = getY() + e.getDistance() * Math.cos(Math.toRadians(absBearing));
        enemy.heading = e.getHeading();
        enemy.energy = e.getEnergy();
        
        // Velocity components
        double newVx = e.getVelocity() * Math.sin(Math.toRadians(e.getHeading()));
        double newVy = e.getVelocity() * Math.cos(Math.toRadians(e.getHeading()));
        
        if (enemy.vx == 0 && enemy.vy == 0) {
            enemy.vx = newVx;
            enemy.vy = newVy;
        } else {
            // Smooth velocity tracking
            enemy.vx = enemy.vx * 0.8 + newVx * 0.2;
            enemy.vy = enemy.vy * 0.8 + newVy * 0.2;
        }
        
        // Firing logic
        if (e.getName().equals(primaryTarget)) {
            fireAtTarget(e, absBearing);
        }
        
        // Movement strategy
        performMovement(e);
    }
    
    /**
     * Advanced circular/linear targeting with bullet travel time compensation
     */
    private void fireAtTarget(ScannedRobotEvent e, double absBearing) {
        double distance = e.getDistance();
        double myEnergy = getEnergy();
        
        // Adaptive fire power
        double firePower;
        if (e.getEnergy() < 25 && distance < 250) {
            // Finish blow
            firePower = Math.min(3.0, e.getEnergy() / 4 + 0.5);
        } else if (distance < 150) {
            firePower = 2.5;
        } else if (distance < 400) {
            firePower = Math.min(OPTIMAL_FIRE_POWER, myEnergy / 6);
        } else {
            firePower = Math.min(1.5, myEnergy / 8);
        }
        
        firePower = Math.max(0.5, Math.min(3.0, firePower));
        
        // Calculate bullet travel time
        double bulletVelocity = 20.0 - (3.0 * firePower);
        double timeToHit = distance / bulletVelocity;
        
        // Predict future position (circular + linear combined)
        double predX = predictTargetX(e, timeToHit, absBearing);
        double predY = predictTargetY(e, timeToHit, absBearing);
        
        // Calculate angle to predicted position
        double angleToTarget = Math.atan2(predX - getX(), predY - getY());
        double gunTurn = normalizeBearing(Math.toDegrees(angleToTarget) - getGunHeading());
        
        setTurnGunRight(gunTurn);
        
        // Fire if gun is nearly aligned (reduces energy waste)
        if (Math.abs(gunTurn) < 15) {
            setFire(firePower);
        }
    }
    
    /**
     * Predict X coordinate accounting for velocity
     */
    private double predictTargetX(ScannedRobotEvent e, double time, double bearing) {
        Enemy enemy = enemies.get(e.getName());
        if (enemy == null) return e.getDistance() * Math.sin(Math.toRadians(bearing));
        
        double currentX = getX() + e.getDistance() * Math.sin(Math.toRadians(bearing));
        return currentX + (enemy.vx * time);
    }
    
    /**
     * Predict Y coordinate accounting for velocity
     */
    private double predictTargetY(ScannedRobotEvent e, double time, double bearing) {
        Enemy enemy = enemies.get(e.getName());
        if (enemy == null) return e.getDistance() * Math.cos(Math.toRadians(bearing));
        
        double currentY = getY() + e.getDistance() * Math.cos(Math.toRadians(bearing));
        return currentY + (enemy.vy * time);
    }
    
    /**
     * Sophisticated movement combining multiple strategies
     */
    private void performMovement(ScannedRobotEvent e) {
        double distance = e.getDistance();
        long now = getTime();
        
        // Strategy selection based on distance
        if (distance < 100) {
            // Close range: evasive maneuvering
            if (now % 60 < 30) {
                setAhead(200);
            } else {
                setBack(200);
            }
            setTurnRight(90 + (Math.random() - 0.5) * 60);
            
        } else if (distance < 300) {
            // Medium range: controlled orbit
            double angleToEnemy = e.getBearing();
            if (angleToEnemy > 0) {
                setTurnLeft(90);
            } else {
                setTurnRight(90);
            }
            setAhead(400);
            
        } else {
            // Long range: approach while dodging
            if (now % 80 < 40) {
                setAhead(600);
            } else {
                setAhead(300);
            }
            
            // Avoid walls
            avoidWalls();
        }
    }
    
    /**
     * Wall avoidance system
     */
    private void avoidWalls() {
        double margin = 80;
        double x = getX();
        double y = getY();
        double width = getBattleFieldWidth();
        double height = getBattleFieldHeight();
        
        if (x < margin || x > width - margin || y < margin || y > height - margin) {
            double centerX = width / 2;
            double centerY = height / 2;
            
            double angleToCenter = Math.atan2(centerX - x, centerY - y);
            double turn = normalizeBearing(Math.toDegrees(angleToCenter) - getHeading());
            
            setTurnRight(turn);
            setAhead(300);
        }
    }
    
    /**
     * Aggressive response to incoming fire
     */
    public void onHitByBullet(HitByBulletEvent e) {
        setBack(150);
        setTurnRight(normalizeBearing(e.getBearing() + 90));
    }
    
    /**
     * Robot collision handling
     */
    public void onHitRobot(HitRobotEvent e) {
        if (!e.isMyFault()) {
            setAhead(100);
        } else {
            setBack(100);
        }
    }
    
    /**
     * Wall collision avoidance
     */
    public void onHitWall(HitWallEvent e) {
        setBack(200);
        setTurnRight(normalizeBearing(e.getBearing() + 180 + (Math.random() - 0.5) * 60));
    }
    
    /**
     * Death tracking - remove dead enemies
     */
    public void onRobotDeath(RobotDeathEvent e) {
        enemies.remove(e.getName());
    }
    
    /**
     * Normalize angle to -180 to 180 range
     */
    private double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
}