package seventeen;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotBeaver extends Robot {
    RobotBeaver(RobotController rc) { super(rc); }

    // How much supply the Beaver would like to have before he leaves to explore the world
    public static final int STARTING_SUPPLY = 1000;

    private static final int BARRACKS_BUILD_DISTSQ = 25;

    private MapLocation hqLoc;

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBeaver");
        hqLoc = rc.senseHQLocation();

        // Wait for supplies
        while (rc.getSupplyLevel() <= STARTING_SUPPLY / 2) {
            rc.yield();
        }

        // Main loop
        while(true) {
            rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

            int distanceFromHQ = rc.getLocation().distanceSquaredTo(hqLoc);
            if (rc.isCoreReady() && distanceFromHQ >= BARRACKS_BUILD_DISTSQ) {
                buildBarracks();
            }

            if (rc.isCoreReady()) mine();

            if (rc.isCoreReady()) wander();

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
