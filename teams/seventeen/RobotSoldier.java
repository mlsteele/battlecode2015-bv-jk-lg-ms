package seventeen;

import battlecode.common.*;
import battlecode.common.MapLocation;

import java.util.*;

public class RobotSoldier extends Robot {
    RobotSoldier(RobotController rc) { super(rc); }

    public static final int STARTING_SUPPLY = 1000;

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotSoldier");

        // Go to HQ then wait for supplies
        MapLocation our_hq = rc.senseHQLocation();
        MapLocation wait_here_for_supplies = our_hq.add(0,1);
        //while (rc.getSupplyLevel() <= STARTING_SUPPLY / 2) {
        //    if (rc.getLocation() != wait_here_for_supplies && rc.isCoreReady())
        //        moveToward(wait_here_for_supplies);
        //    else rc.yield();
        //}

        // Main loop
        while(true) {
            rf.loadRally1();
            rc.setIndicatorString(1, "rallying to " + rf.getRally1());
            shootBaddies();
            if (rc.isCoreReady()) moveToward(rf.getRally1());

            rc.yield();
        }
    }
}
