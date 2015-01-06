package miles;

import battlecode.common.*;
import java.util.*;

public class RobotPlayer {
    public static void run(RobotController rc) {
        Robot r;

        rc.setIndicatorString(0, "having existential crisis.");

        switch (rc.getType()) {
            case HQ:
                r = new RobotHQ(rc);
                r.run();
                break;
            default:
                // Unimplemented robot type
                rc.yield();
        }
        while(true) {
            rc.yield();
        }
    }
}
