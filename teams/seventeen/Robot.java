package seventeen;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import java.util.*;

// Base class for Robot minds.
// Stores the RobotController as an instance object.
// Contains helper methods commonly useful to robots.
public abstract class Robot {
    protected RobotController rc;
    protected RadioFrob rf;
    protected Random rand;
    protected Direction forward;

    Robot(RobotController rc) {
        this.rc = rc;
        rf = new RadioFrob(rc);
        rand = new Random(rc.getID());
        forward = randomDirection();
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

    protected boolean safeSpawn(Direction dir, RobotType rtype) {
        if (rc.canSpawn(dir, rtype)) {
            try {
                rc.spawn(dir, rtype);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    // Tries to move dir.
    // Returns whether successful.
    // Assumes CoreReady
    protected boolean moveForward() {
        if (rc.canMove(forward)) {
            try {
                rc.move(forward);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                if((rand.nextDouble() * 2) <= 1) {
                    forward = forward.rotateRight();
                } else {
                    forward = forward.rotateLeft();
                }
                if (rc.canMove(forward)) {
                    rc.move(forward);
                }
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }

        }
    }

    protected boolean moveToward(MapLocation loc) {
        forward = rc.getLocation().directionTo(loc);
        return moveForward();
    }

    // Wander the field aimlessly.
    // Returns whether movement occurred.
    // Assumes CoreReady
    // TOOD(miles): If this gets used, please avoid walking into the line of fire.
    protected boolean wander() {
        // MapLocation target = rc.getLocation().add(forward);
        for (int i = 0; i < 4; i++) {
            if (moveForward()) {
                return true;
            } else {
                forward = forward.rotateRight().rotateRight();
            }
        }
        return false;
    }

    protected boolean shootBaddies() {
        if (rc.isWeaponReady()) {
            int range = rc.getType().attackRadiusSquared;
            Team enemy = rc.getTeam().opponent();
            RobotInfo[] enemies = rc.senseNearbyRobots(range, enemy);
            // TODO(miles): What's the poitn of canAttackLocation?
            if (enemies.length > 0) {
                try {
                    rc.attackLocation(chooseTarget(enemies).location);
                    return true;
                } catch (GameActionException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }
}
