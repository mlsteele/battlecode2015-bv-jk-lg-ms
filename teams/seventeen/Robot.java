package miles;

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
}
