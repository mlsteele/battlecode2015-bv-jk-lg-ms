package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class Beaver extends Unit {
    Beaver(RobotController rc) { super(rc); }

    private static final int LOW_SUPPLY = 50;

    // Require this distance free space around buildings
    private static final int BUILDING_PADDING = 5;

    // Don't build farther away than this
    private static final int MAX_DISTANCE_FROM_HQ = 100;

    private Task currentTask = new Task(Strategy.TASK_NONE);

    @Override
    public void run() {
        int myTaskSlot = rf.getBeaverTaskSlot();
        rf.myTaskSlot = myTaskSlot;

        currentTask = rf.getTask(myTaskSlot);
        rc.setIndicatorString(0, "slot:" + myTaskSlot + " task:" + currentTask.taskNum);

        // This is NOT the inner loop.
        while (true) {

            int initialSupplyLevel = waitForSupplies();

            rc.setIndicatorString(0, "slot:" + rf.myTaskSlot + " task:" + currentTask.taskNum);

            // Order code is which mission to pursue.
            int orderCode = currentTask.taskNum;

            switch (orderCode) {
                case (Strategy.TASK_BARRACKS):
                case (Strategy.TASK_MINERFACTORY):
                case (Strategy.TASK_TANKFACTORY):
                case (Strategy.TASK_HELIPAD):
                case (Strategy.TASK_SUPPLYDEPOT):
                    buildStructureMission();
                    break;
                case (Strategy.TASK_RESUPPLY_TANKFACTORY):
                    resupplyMission();
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
                    System.err.println("ERROR: BEAVER sent on invalid mission ("+orderCode+"), please debug");
            }

            // Finished what it was doing
            rc.setIndicatorString(1, "finished task");
            System.out.println("BEAVER finished task " + orderCode);
            currentTask = new Task(Strategy.TASK_BARRACKS);
            goToHQ();
            dumpSuppliesToHQ();
            getTaskFromHQ();
            // Just give up and die.
            //rc.disintegrate();
        }
    }

    private void buildStructureMission() {
        while (true) {
            int distanceFromHQ = rc.getLocation().distanceSquaredTo(hqLoc);

            if (rc.isCoreReady()) {
                if (distanceFromHQ > MAX_DISTANCE_FROM_HQ) {
                    moveToward(hqLoc);
                } else {
                    if (isClearToBuild()) {
                        if (buildThenSupplyForCode(currentTask.taskNum)) {
                            return;
                        }
                    } else {
                        wander();
                    }
                }
            }
            rc.yield();
        }
    }

    private boolean isClearToBuild() {
        RobotInfo[] nearby = rc.senseNearbyRobots(BUILDING_PADDING);
        for (RobotInfo r : nearby) {
            if (r.type.isBuilding || r.team == rc.getTeam().opponent()) {
                return false;
            }
        }
        return true;
    }

    private void resupplyMission() {
        int amt;
        switch (currentTask.taskNum) {
            case (Strategy.TASK_RESUPPLY_TANKFACTORY):
                amt = Strategy.TANKFACTORY_RESUPPLY_AMT;
                break;
            default:
                throw new NotImplementedException("Unknown resupply mission");

        }
        while(true) {
            if (rc.isCoreReady()) {
                if (rc.getLocation().distanceSquaredTo(currentTask.loc) > GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
                    moveToward(currentTask.loc);
                    rc.yield();
                    continue;
                } else {
                    try {
                        rc.transferSupplies(amt, currentTask.loc);
                        rc.yield();
                        return;
                    } catch (GameActionException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            } else {
                rc.yield();
            }
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
            case Strategy.TASK_SUPPLYDEPOT:
                return buildThenSupply(SUPPLYDEPOT);
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
                MapLocation buildLoc = rc.getLocation().add(dir);
                rc.yield(); // Wait one turn for the building to spawn.
                rc.transferSupplies(supply, buildLoc);
                waitForBuildCompletion(buildLoc);
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
            case SUPPLYDEPOT:
                supply = Strategy.initialSupply(SUPPLYDEPOT);
                break;
            default:
                supply = 0;
        }
        return buildThenSupply(rob, supply);
    }

    // Wait (blocking) for a structure to finish being built by this robot.
    // If the building dies, that counts too.
    private void waitForBuildCompletion(MapLocation loc) {
        RobotInfo ri = null;
        do {
            try {
                ri = rc.senseRobotAtLocation(loc);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            // Poll the building's builder field.
            if (ri != null && ri.builder == null) {
                return;
            }
            rc.yield();
        } while (true);
    }

    // gets a tack from the hq
    // blocking
    private void getTaskFromHQ() {
        goToHQ();

        // im near the hq, lets ask for a task and clear my task slot
        rf.requestTask();
        currentTask = new Task(Strategy.TASK_REQUESTING_TASK);

        // wait for supply from HQ
        while(rc.getSupplyLevel() < Strategy.initialSupply(RobotType.BEAVER)) rc.yield();
        currentTask = rf.getTask(rf.myTaskSlot);
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
