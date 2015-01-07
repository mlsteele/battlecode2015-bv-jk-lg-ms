package seventeen;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotBeaver extends Robot {
    public static final int STARTING_SUPPLY = 1000;

    private static final int BARRACKS_BUILD_DIST = 4;

    private int distanceTraveled = 0;

    RobotBeaver(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBeaver");

        // Wait for supplies
        while (rc.getSupplyLevel() <= STARTING_SUPPLY / 2) {
            rc.yield();
        }

        // Main loop
        while(true) {
            rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

            if (rc.isCoreReady() && distanceTraveled >= BARRACKS_BUILD_DIST) {
                buildBarracks();
            }

            if (rc.isCoreReady()) mine();

            if (rc.isCoreReady()) {
                if (wander()) {
                    distanceTraveled += 1;
                }
            }

            rc.yield();
        }
    }

    // Attempt to mine.
    // NOTE: this is beaver specific because of that constant.
    private void mine() {
        boolean shouldMine = rc.senseOre(rc.getLocation()) >= GameConstants.BEAVER_MINE_MAX;
        if (shouldMine && rc.canMine()) {
            try {
                rc.mine();
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }

    // Attempt to build a barracks.
    private void buildBarracks() {
        Direction dir = randomDirection();
        if (rc.canBuild(dir, BARRACKS)) {
            try {
                rc.build(dir, BARRACKS);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }
}
