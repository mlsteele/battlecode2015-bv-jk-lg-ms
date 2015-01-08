package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.RobotType.*;

public class Beaver extends Unit {
    Beaver(RobotController rc) { super(rc); }

    private static final int LOW_SUPPLY = 50;

    // Require this distance free space around buildings
    private static final int BUILDING_PADDING = 25;

    private MapLocation hqLoc;
    private int currentJob = 0;

    @Override
    public void run() {
        try {
            int myJobSlot = rf.getBeaverJobSlot();
            rf.clearBeaverJobSlot();
            rf.myJobSlot = myJobSlot;

            currentJob = rf.getJob();
            rc.setIndicatorString(0, "i am Beaver : My job slot is " + myJobSlot + " and my job is : " + currentJob);
        } catch (GameActionException e) {
            e.printStackTrace();
        }


        hqLoc = rc.senseHQLocation();
        
        // Not your average loop.
        // This is NOT the inner loop.
        while (true) {

            int initialSupplyLevel = waitForSupplies();

            // Order code is which mission to pursue.
            int orderCode = initialSupplyLevel % 100;

            rc.setIndicatorString(1, "BEAVER mission " + orderCode);
            System.out.println("BEAVER mission " + orderCode);
            switch (orderCode) {
                case (Headquarters.ORDER_BARRACKS):
                case (Headquarters.ORDER_MINERFACTORY):
                case (Headquarters.ORDER_TANKFACTORY):
                    buildStructureMission(orderCode);
                    break;
                case (Headquarters.ORDER_NONE):
                    System.out.println("BEAVER mission none");
                    while (true) {
                        rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

                        if (rc.getSupplyLevel() < LOW_SUPPLY) break;

                        if (rc.isCoreReady()) mine();
                        if (rc.isCoreReady()) wander();

                        rc.yield();
                    }
                    break;
                default:
                    System.out.println("ERROR: BEAVER sent on invalid mission ("+orderCode+"), please debug");
            }

            // Finished what it was doing
            rc.setIndicatorString(1, "Finished mission " + orderCode);
            goToHQ();
            dumpSuppliesToHQ();
            // Just give up and die.
            rc.disintegrate();
        }
    }

    private void buildStructureMission(int orderCode) {
        while (true) {
            // TODO(jessk) Make sure no buildings are nearby before building here
            int distanceFromHQ = rc.getLocation().distanceSquaredTo(hqLoc);
            rc.setIndicatorString(2, "distanceFromHQ: " + distanceFromHQ);
            if (rc.isCoreReady()) {
                if (distanceFromHQ >= BUILDING_PADDING && buildThenSupplyForCode(orderCode))
                    return;
                wander();
            }

            rc.yield();
        }
    }

    private boolean buildThenSupplyForCode(int orderCode) {
        switch (orderCode) {
            case Headquarters.ORDER_BARRACKS:
                return buildThenSupply(BARRACKS);
            case Headquarters.ORDER_MINERFACTORY:
                return buildThenSupply(MINERFACTORY);
            case Headquarters.ORDER_TANKFACTORY:
                return buildThenSupply(TANKFACTORY);
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
    private boolean buildThenSupply(RobotType rob, int supply) {
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
                supply = Strategy.initialSupply(BARRACKS);
                break;
            case MINERFACTORY:
                supply = Strategy.initialSupply(MINERFACTORY);
                break;
            case TANKFACTORY:
                supply = Strategy.initialSupply(TANKFACTORY);
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

            if(hqLoc.distanceSquaredTo(rc.getLocation()) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
                rc.setIndicatorString(1, "Back at HQ");
                return true;
            }

            rc.yield();
        }
    }

    private void dumpSuppliesToHQ() {
        rc.setIndicatorString(1, "Dumping supplies...");
        try {
            rc.transferSupplies(Integer.MAX_VALUE, hqLoc);
            rc.setIndicatorString(1, "Dumped supplies.");
            return;
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
