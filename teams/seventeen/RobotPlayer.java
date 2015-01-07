package seventeen;

import battlecode.common.*;
import java.util.*;

// Entry point for robots.
public class RobotPlayer {
    public static void run(RobotController rc) {
        Robot r;

        // Dispatch to one of the Robot* classes.
        // Should never return.
        switch (rc.getType()) {
            case HQ:
                r = new RobotHQ(rc);
                r.run();
                break;
            case TOWER:
                r = new RobotTower(rc);
                r.run();
                break;
            case BEAVER:
                r = new RobotBeaver(rc);
                r.run();
                break;
            case BARRACKS:
                r = new RobotBarracks(rc);
                r.run();
                break;
            case SOLDIER:
                r = new RobotSoldier(rc);
                r.run();
            default:
                // Unimplemented robot type
                rc.setIndicatorString(0, "having existential crisis.");
                while (true) {
                    rc.yield();
                }
        }
    }
}
