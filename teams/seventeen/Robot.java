package seventeen;

import battlecode.common.*;
import java.util.*;

// Base class for Robot minds.
// Stores the RobotController as an instance object.
// Contains helper methods commonly useful to robots.
public abstract class Robot {
    protected RobotController rc;

    Robot(RobotController rc) {
        this.rc = rc;
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

    protected boolean safeMove(Direction dir) {
        if (rc.canMove(dir)) {
            try {
                rc.move(dir);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
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
