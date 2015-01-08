package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;

import static battlecode.common.RobotType.*;

public class RobotBeaver extends Robot {
    RobotBeaver(RobotController rc) { super(rc); }

    // How much supply the Beaver would like to have before he leaves to explore the world
    public static final int STARTING_SUPPLY = 1000;
    private static final int LOW_SUPPLY = 50;

    // Require this distance free space around buildings
    private static final int BUILDING_PADDING = 25;

    private MapLocation hqLoc;

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBeaver");
        hqLoc = rc.senseHQLocation();
        
        // Not your average loop.
        // This is NOT the inner loop.
        while (true) {

            waitForSupplies();

            // Order code is which mission to pursue.
            int orderCode = ((int) rc.getSupplyLevel()) % 100;

            switch (orderCode) {
                case (RobotHQ.ORDER_MINERFACTORY):
                    buildStructureMission(RobotHQ.ORDER_BARRACKS);
                    break;
                case (RobotHQ.ORDER_BARRACKS):
                    buildStructureMission(RobotHQ.ORDER_BARRACKS);
                    break;
                default:
                    System.out.println("BEAVER mission none");
                    while (true) {
                        rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

                        if (rc.getSupplyLevel() < LOW_SUPPLY) break;

                        if (rc.isCoreReady()) mine();
                        if (rc.isCoreReady()) wander();

                        rc.yield();
                    }
                    break;
            }

            // Finished what it was doing
            goToHQ();
            dumpSuppliesToHQ();
        }
    }

    private void buildStructureMission(int orderCode) {
        System.out.println("BEAVER mission " + orderCode);
        rc.setIndicatorString(1, "BEAVER mission " + orderCode);
        while (true) {
            rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

            // TODO(jessk) Make sure no buildings are nearby before building here
            int distanceFromHQ = rc.getLocation().distanceSquaredTo(hqLoc);
            if (rc.isCoreReady()) {
                if (distanceFromHQ >= BUILDING_PADDING && buildThenSupplyForCode(orderCode)) break;
                wander();
            }

            rc.yield();
        }
        rc.setIndicatorString(1, "Finished mission " + orderCode);
    }

    private boolean buildThenSupplyForCode(int orderCode) {
        switch (orderCode) {
            case RobotHQ.ORDER_BARRACKS:
                return buildThenSupply(BARRACKS);
            case RobotHQ.ORDER_MINERFACTORY:
                return buildThenSupply(MINERFACTORY);
            default:
                System.out.println("error, invalid building code " + orderCode);
                return false;
        }
    }

    // Attempt to mine.
    // NOTE: this is beaver specific because of that constant.
    private boolean mine() {
        boolean shouldMine = rc.senseOre(rc.getLocation()) >= GameConstants.BEAVER_MINE_MAX;
        if (shouldMine && rc.canMine()) {
            try {
                rc.mine();
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return false;
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
            case TANKFACTORY:
                supply = RobotTankFactory.STARTING_SUPPLY;
                break;
            default:
                supply = 0;
        }
        return buildThenSupply(rob, supply);
    }

    private boolean goToHQ() {
        rc.setIndicatorString(1, "Going back to HQ");
        while(true) {

            if(rc.isCoreReady()) moveToward(hqLoc);

            if(hqLoc.distanceSquaredTo(rc.getLocation()) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED)
                return true;

            rc.yield();

        }
    }

    private boolean dumpSuppliesToHQ() {
        while(true) {
            if (rc.isCoreReady()) {
                try {
                    rc.transferSupplies((int) rc.getSupplyLevel(), hqLoc);
                    return true;
                } catch (GameActionException e) {
                    return false;
                }
            }
            rc.yield();
        }
    }
}
