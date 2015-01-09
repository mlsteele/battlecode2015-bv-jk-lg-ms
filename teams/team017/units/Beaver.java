package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.RobotType.*;

public class Beaver extends Unit {
    Beaver(RobotController rc) { super(rc); }

    private static final int LOW_SUPPLY = 50;

    // Require this distance free space around buildings
    private static final int BUILDING_PADDING = 25;

    // Don't build farther away than this
    private static final int MAX_DISTANCE_FROM_HQ = 100;

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

        // This is NOT the inner loop.
        while (true) {

            int initialSupplyLevel = waitForSupplies();

            // Order code is which mission to pursue.
            //int orderCode = initialSupplyLevel % 100;
            int orderCode = currentJob;

            rc.setIndicatorString(1, "BEAVER mission " + orderCode);
            System.out.println("BEAVER mission " + orderCode);
            switch (orderCode) {
                case (Strategy.TASK_BARRACKS):
                case (Strategy.TASK_MINERFACTORY):
                case (Strategy.TASK_TANKFACTORY):
                case (Strategy.TASK_HELIPAD):
                    buildStructureMission(orderCode);
                    break;
                case (Strategy.TASK_NONE):
                    System.out.println("BEAVER mission none");
                    while (true) {
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
            System.out.println("BEAVER finished mission " + orderCode);
            goToHQ();
            dumpSuppliesToHQ();
            // Just give up and die.
            rc.disintegrate();
        }
    }

    private void buildStructureMission(int orderCode) {
        whileLoop: while (true) {
            int distanceFromHQ = rc.getLocation().distanceSquaredTo(hqLoc);
            if (rc.isCoreReady()) {
                if (distanceFromHQ > MAX_DISTANCE_FROM_HQ) {
                    moveToward(hqLoc);
                } else {
                    RobotInfo[] nearby = rc.senseNearbyRobots(BUILDING_PADDING);
                    for (RobotInfo r : nearby) {
                        if (r.type.isBuilding || r.team == rc.getTeam().opponent()) {
                            // can't build here
                            wander();
                            rc.yield();
                            continue whileLoop;
                        }
                    }
                    // ok, we can build
                    if (buildThenSupplyForCode(orderCode)) return;
                    else {
                        wander();
                        rc.yield();
                        continue whileLoop;
                    }
                }
            }
            rc.yield();
        }
    }

    private boolean buildThenSupplyForCode(int orderCode) {
        switch (orderCode) {
            case Strategy.TASK_BARRACKS:
                return buildThenSupply(BARRACKS);
            case Strategy.TASK_MINERFACTORY:
                return buildThenSupply(MINERFACTORY);
            case Strategy.TASK_TANKFACTORY:
                return buildThenSupply(TANKFACTORY);
            case Strategy.TASK_HELIPAD:
                return buildThenSupply(HELIPAD);
            default:
                System.err.println("error, invalid building code " + orderCode);
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
            case HELIPAD:
                supply = Strategy.initialSupply(HELIPAD);
                break;
            default:
                supply = 0;
        }
        return buildThenSupply(rob, supply);
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
