package team017;

import battlecode.common.*;
import battlecode.common.MapLocation;

import java.util.*;

public class Basher extends Unit {
    Basher(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a Basher");

        waitForSupplies();

        // Main loop
        while (true) {
            rf.loadRally1();
            if (rc.isCoreReady()) moveToward(rf.getRally1());

            rc.yield();
        }
    }
}