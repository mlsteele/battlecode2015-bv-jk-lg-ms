package seventeen;

import battlecode.common.*;
import static battlecode.common.RobotType.*;

public class RobotBeaver extends Robot {
    RobotBeaver(RobotController rc) { super(rc); }

    // How much supply the Beaver would like to have before he leaves to explore the world
    public static final int STARTING_SUPPLY = 1000;

    // Require this distance free space around buildings
    private static final int BUILDING_PADDING = 25;

    // Give this to the barracks when you build it
    public static final int SUPPLY_FOR_BARRACKS = 10000;

    private MapLocation hqLoc;

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBeaver");
        hqLoc = rc.senseHQLocation();

        // Wait for supplies
        while (rc.getSupplyLevel() <= STARTING_SUPPLY / 2) {
            rc.yield();
        }

        // I am on a barracks mission. Go to build a barracks first
        if (rc.getSupplyLevel() >= SUPPLY_FOR_BARRACKS) {
            rc.setIndicatorString(1, "I am on a barracks mission");
            while (true) {
                rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

                // TODO(jessk) Make sure no buildings are nearby before building here
                int distanceFromHQ = rc.getLocation().distanceSquaredTo(hqLoc);
                if (rc.isCoreReady()) {
                    if (distanceFromHQ >= BUILDING_PADDING && buildBarracks()) break;
                    wander();
                }

                rc.yield();
            }
            rc.setIndicatorString(1, "Finished barracks mission");
        }

        // Main loop... mine & wander
        while(true) {
            rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

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
    private boolean buildBarracks() {
        Direction dir = randomDirection();
        if (rc.canBuild(dir, BARRACKS)) {
            try {
                rc.build(dir, BARRACKS);
                MapLocation barracks_loc = rc.getLocation().add(dir);
                rc.yield();
                rc.transferSupplies(SUPPLY_FOR_BARRACKS, barracks_loc);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
