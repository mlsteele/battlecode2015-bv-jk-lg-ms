package team017;

import battlecode.common.*;
import battlecode.common.MapLocation;

import java.util.*;

public class Soldier extends Unit {
    Soldier(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a Soldier");

        waitForSupplies();

        // Main loop
        while(true) {
            shootBaddies();

            rf.loadRally1();
            if (rc.isCoreReady()) moveToward(rf.getRally1());

            rc.yield();
        }
    }
}
