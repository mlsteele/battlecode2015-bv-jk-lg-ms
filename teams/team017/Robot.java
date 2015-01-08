package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
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

    abstract public void run();

    // Chooses the robot
    private static RobotInfo chooseTarget(RobotInfo[] enemies) {
        RobotInfo best = enemies[0];
        for (RobotInfo rob : enemies) {
            if (rob.health < best.health) {
                best = rob;
            }
        }
        return best;
    }

    protected Direction randomDirection() {
        switch (Math.abs(rand.nextInt()) % 8) {
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

    protected boolean shootBaddies() {
        // Keep scanning until either a hit or no enemies in sight.
        // It's not good to shuffle around, causing further loading delay,
        // when an enemy is in range.
        while (true) {
            int range = rc.getType().attackRadiusSquared;
            Team enemy = rc.getTeam().opponent();
            RobotInfo[] enemies = rc.senseNearbyRobots(range, enemy);

            if (enemies.length == 0) {
                // All clear.
                return false;
            }

            if (rc.isWeaponReady()) {
                try {
                    // Note: canAttackLocation seems useless (see engine source)
                    rc.attackLocation(chooseTarget(enemies).location);
                    return true;
                } catch (GameActionException e) {
                    e.printStackTrace();
                    return false;
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

        rc.setIndicatorString(1, "Waiting for supplies at " + rc.getSupplyLevel());
        boolean shouldAttack = (rc.getType() != BASHER) && (rc.getType() != BEAVER);
        while ((supplyLevel = rc.getSupplyLevel()) < 1) {
            if (shouldAttack) shootBaddies();
            rc.yield();
        }
        System.out.println("Received " + supplyLevel + " supplies");
        rc.setIndicatorString(1, "recvd " + supplyLevel + "supplies");
        return (int)supplyLevel;
    }

}
