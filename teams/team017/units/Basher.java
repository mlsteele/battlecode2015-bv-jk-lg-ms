package team017;

import battlecode.common.*;
import battlecode.common.MapLocation;

import java.util.*;

public class Basher extends Robot {
    Basher(RobotController rc) { super(rc); }

    public static final int STARTING_SUPPLY = 1000;
    private static final int TIME_TO_ATTACK = 500;

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a Basher");

        waitForSupplies();

        // Main loop
        while(true) {
            rf.loadRally1();
            if (rc.isCoreReady()) moveToward(rf.getRally1());

            rc.yield();
        }
    }
}
