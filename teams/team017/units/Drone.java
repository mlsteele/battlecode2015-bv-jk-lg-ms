package team017;

import battlecode.common.*;
import battlecode.common.MapLocation;

import java.util.*;

public class Drone extends Unit {
    Drone(RobotController rc) { super(rc); }

    @Override
    public void run() {
        waitForSupplies();

        // Main loop
        while (true) {
            shootBaddies();

            rf.loadRally1();
            if (rc.isCoreReady()) moveToward(rf.getRally1());

            rc.yield();
        }
    }
}
