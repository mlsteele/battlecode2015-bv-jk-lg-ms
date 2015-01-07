package team017;

import battlecode.common.*;
import static battlecode.common.RobotType.*;

public class RobotBeaver extends Robot {
    RobotBeaver(RobotController rc) { super(rc); }

    // How much supply the Beaver would like to have before he leaves to explore the world
    public static final int STARTING_SUPPLY = 1000;

    // Require this distance free space around buildings
    private static final int BUILDING_PADDING = 25;

    private MapLocation hqLoc;

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBeaver");
        hqLoc = rc.senseHQLocation();

        // Wait for supplies
        while (rc.getSupplyLevel() <= STARTING_SUPPLY / 2) {
            rc.yield();
        }
        //rc.setIndicatorString(1,"ready");

        // I am on a mining factory mission
        if (rc.getSupplyLevel() >= RobotMinerFactory.STARTING_SUPPLY) {
            rc.setIndicatorString(1, "I am on a mining factory mission");
            while (true) {
                rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

                // TODO(jessk) Make sure no buildings are nearby before building here
                int distanceFromHQ = rc.getLocation().distanceSquaredTo(hqLoc);
                if (rc.isCoreReady()) {
                    if (distanceFromHQ >= BUILDING_PADDING && buildThenSupply(MINERFACTORY)) break;
                    wander();
                }

                rc.yield();
            }
            rc.setIndicatorString(1, "Finished mining mission");
        }

        // I am on a barracks mission. Go to build a barracks first
        if (rc.getSupplyLevel() >= RobotBarracks.STARTING_SUPPLY) {
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

    // Attempt to build and then supply a building
    private boolean buildThenSupply(RobotType rob , int supply) {
        Direction dir = randomDirection();
        if (rc.canBuild(dir, rob)) {
            try {
                rc.build(dir, rob);
                MapLocation rob_loc = rc.getLocation().add(dir);
                // Wait one turn for the building to spawn.
                rc.yield();
                rc.transferSupplies(supply, rob_loc);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Gives the default starting supply to contructed building
    private boolean buildThenSupply(RobotType rob) {
        int supply = 0;
        switch (rob) {
            case BARRACKS:
                supply = RobotBarracks.STARTING_SUPPLY;
                break;
            case MINERFACTORY:
                supply = RobotMinerFactory.STARTING_SUPPLY;
                break;
            default:
                supply = 0;
        }
        return buildThenSupply(rob, supply);
    }

    // Attempt to build a barracks.
    private boolean buildBarracks() {
        return buildThenSupply(BARRACKS);
    }
}
