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

    private Task currentTask = new Task(TASK_NONE);

    @Override
    public void run() {
        rf.myTaskSlot = rf.getBeaverTaskSlot();

        currentTask = rf.getTask(rf.myTaskSlot);
        rc.setIndicatorString(0, "slot:" + rf.myTaskSlot + " task:" + currentTask.taskNum);
        //System.out.println("BEAVER initial task " + currentTask.taskNum);

        waitForSupplies();

        // This is NOT the inner loop.
        while (true) {

            rc.setIndicatorString(0, "slot:" + rf.myTaskSlot + " task:" + currentTask.taskNum);

            // Order code is which task to pursue.
            int orderCode = currentTask.taskNum;

            switch (orderCode) {
                case (TASK_BARRACKS):
                case (TASK_MINERFACTORY):
                case (TASK_TANKFACTORY):
                case (TASK_HELIPAD):
                case (TASK_SUPPLYDEPOT):
                    buildStructureMission();
                    break;
                case (TASK_RESUPPLY_TANKFACTORY):
                    resupplyMission();
                    break;
                case (TASK_MINE):
                    while (true) {
                        if (rc.getSupplyLevel() < LOW_SUPPLY) break;

                        if (rc.isCoreReady()) mine();
                        if (rc.isCoreReady()) wander();

                        rc.yield();
                    }
                    break;
                case (TASK_NONE):
                    break;
                default:
                    System.err.println("ERROR: BEAVER sent on invalid mission ("+orderCode+"), please debug");
            }

            // Finished what it was doing
            rc.setIndicatorString(1, "finished task");
            //System.out.println("BEAVER finished task " + orderCode);
            goToHQ();
            rc.setIndicatorString(1, "returned to hq");
            dumpSuppliesToHQ();
            getTaskFromHQ();
        }
    }

    private void buildStructureMission() {
        while (true) {
            int distanceFromHQ = rc.getLocation().distanceSquaredTo(hqLoc);

            if (rc.isCoreReady()) {
                if (distanceFromHQ > MAX_DISTANCE_FROM_HQ) {
                    moveTowardBugging(hqLoc);
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
                    moveTowardBugging(currentTask.loc);
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

    // Requests a task from HQ.
    // Blocks until we have a new task.
    private void getTaskFromHQ() {
        goToHQ();

        rc.setIndicatorString(1, "awaiting new task");
        currentTask = null;

        // im near the hq, lets ask for a task and clear my task slot
        // wait for signal from HQ
        do {
            rf.requestTask();
            // yield so the request propogates, otherwise we might see our old task.
            // yield so that we don't request a task while having an assigned task.
            rc.yield();
            currentTask = rf.getTask(rf.myTaskSlot);
        } while (currentTask == null);

        rc.setIndicatorString(1, "received task");
        //System.out.println("BEAVER received task " + currentTask.taskNum);
    }

}
