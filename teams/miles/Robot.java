package miles;

import battlecode.common.*;
import java.util.*;

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
}
