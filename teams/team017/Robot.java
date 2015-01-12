package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;

import java.lang.RuntimeException;
import java.util.*;

// Base class for Robot minds.
// Stores the RobotController as an instance object.
// Contains helper methods commonly useful to robots.
public abstract class Robot {
    protected RobotController rc;
    protected RadioFrob rf;
    protected Random rand;

    Robot(RobotController rc) {
        this.rc = rc;
        rf = new RadioFrob(rc);
        rand = new Random(rc.getID());
    }

    protected static int NUM_ROBOT_TYPES  = RobotType.values().length;

    abstract public void run();

    // Chooses which robot to attack.
    private static RobotInfo chooseTarget(RobotInfo[] enemies) {
        RobotInfo target = enemies[0];
        double    targetScore = -1;
        for (RobotInfo r : enemies) {
            double damage_per_whatever = r.type.attackPower / r.type.attackDelay;
            double score = damage_per_whatever; // 1060
            if (score > targetScore) {
                target = r;
                targetScore = score;
            }
        }
        return target;
    }

    protected Direction randomDirection() {
        switch (rand.nextInt(8)) {
            case 0: return NORTH;
            case 1: return NORTH_EAST;
            case 2: return EAST;
            case 3: return SOUTH_EAST;
            case 4: return SOUTH;
            case 5: return SOUTH_WEST;
            case 6: return WEST;
            case 7: return NORTH_WEST;
        }
        // This should never happen.
        throw new RuntimeException("Something mod 8 is bigger than 7");
    }

    // Attempts to spawn a robot of rtype.
    // Spawns at an arbitrary adjacent square.
    // Returns the direction of spawn, or NULL if no spawn occurred.
    // Assumes CoreReady
    protected Direction spawn(RobotType rtype) {
        Direction dir = randomDirection();
        for (int i = 0; i < 8; i++) {
            if (rc.canSpawn(dir, rtype)) {
                try {
                    rc.spawn(dir, rtype);
                    return dir;
                } catch (GameActionException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            dir = dir.rotateLeft();
        }
        return null;
    }

    protected void shootBaddies() {
        // TODO(miles): attackRadiusSquared is not good for the HQ, must account for range buf
        int range = rc.getType().attackRadiusSquared;
        Team enemy = rc.getTeam().opponent();

        // Keep scanning until either a hit or no enemies in sight.
        // It's not good to shuffle around, causing further loading delay
        // when an enemy is in range.
        while (true) {
            RobotInfo[] enemies = rc.senseNearbyRobots(range, enemy);

            // Return if all clear.
            if (enemies.length == 0)
                return;

            if (rc.isWeaponReady()) {
                try {
                    // Note: canAttackLocation seems useless (see engine source)
                    rc.attackLocation(chooseTarget(enemies).location);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }

            rc.yield();
        }
    }

    // Idle until any supplies are received.
    // Also shoot.
    // Return the amount of supplies received.
    protected int waitForSupplies() {
        double supplyLevel;

        boolean shouldAttack = (rc.getType() != BASHER) && (rc.getType() != BEAVER);
        while ((supplyLevel = rc.getSupplyLevel()) < 1) {
            if (shouldAttack) shootBaddies();
            rc.yield();
        }
        return (int)supplyLevel;
    }

    // Includes HQ in the list of possible locations
    protected MapLocation closestEnemyTowerTo(MapLocation loc) {
        if (loc == null) throw new RuntimeException("closestEnemyTowerTo is given a null location");
        MapLocation[] enemyTowerLocations = rc.senseEnemyTowerLocations();
        MapLocation closestEnemy = rc.senseEnemyHQLocation();
        int closestDistanceSquared = closestEnemy.distanceSquaredTo(loc);
        int thisDistanceSquared;
        for (MapLocation enemyTower : enemyTowerLocations) {
            thisDistanceSquared = enemyTower.distanceSquaredTo(loc);
            if (thisDistanceSquared < closestDistanceSquared) {
                closestDistanceSquared = thisDistanceSquared;
                closestEnemy = enemyTower;
            }
        }
        return closestEnemy;
    }

    // Returns HQ if there are no towers
    protected MapLocation closestTowerTo(MapLocation loc) {
        if (loc == null) throw new RuntimeException("closestEnemyTowerTo is given a null location");
        MapLocation[] towerLocations = rc.senseTowerLocations();
        if (towerLocations.length == 0) return rc.senseHQLocation();
        MapLocation closest = towerLocations[0];
        int closestDistanceSquared = closest.distanceSquaredTo(loc);
        int thisDistanceSquared;
        for (MapLocation tower : towerLocations) {
            thisDistanceSquared = tower.distanceSquaredTo(loc);
            if (thisDistanceSquared < closestDistanceSquared) {
                closestDistanceSquared = thisDistanceSquared;
                closest = tower;
            }
        }
        return closest;
    }

}
