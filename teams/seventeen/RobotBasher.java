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

        // Go to HQ then wait for supplies
        MapLocation our_hq = rc.senseHQLocation();
        MapLocation wait_here_for_supplies = our_hq.add(0,1);
        //while (rc.getSupplyLevel() <= STARTING_SUPPLY / 2) {
        //    if (rc.getLocation() != wait_here_for_supplies && rc.isCoreReady())
        //        moveToward(wait_here_for_supplies);
        //    else rc.yield();
        //}

        // Wait to attack all at once
        // TODO(jessk): go to rally point
        while (Clock.getRoundNum() < TIME_TO_ATTACK) {
            if (rc.isCoreReady()) wander();
            rc.yield();
        }

        // Main loop
        MapLocation enemy_hq = rc.senseEnemyHQLocation();
        while(true) {
            rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

            shootBaddies();

            if (rc.isCoreReady()) {
                moveToward(enemy_hq);
            }

            rc.yield();
        }
    }
}
