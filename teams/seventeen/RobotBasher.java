package seventeen;

import battlecode.common.*;
import battlecode.common.MapLocation;

import java.util.*;

public class RobotBasher extends Robot {
    RobotBasher(RobotController rc) { super(rc); }

    public static final int STARTING_SUPPLY = 1000;
    private static final int TIME_TO_ATTACK = 500;

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBasher");

        waitForSupplies();

        // Main loop
        while(true) {
            rf.loadRally1();
            if (rc.isCoreReady()) moveToward(rf.getRally1());

            rc.yield();
        }
    }
}
