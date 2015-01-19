package droneHarrass.units;

import static battlecode.common.RobotType.*;
import droneHarrass.*;
import battlecode.common.*;

public class Beaver extends Unit {
    public Beaver(RobotController rc) { super(rc); }

    private static final int LOW_SUPPLY = 50;

    // Require this distance free space around buildings
    private static final int BUILDING_PADDING = 5;

    // Don't build farther away than this
    private static final int MAX_DISTANCE_FROM_HQ = 100;

    private int myTaskSlot;
    private Task currentTask;

    @Override
    public void run() {
        myTaskSlot = rf.beavertasks.acquireTaskSlot();
        currentTask = rf.beavertasks.getTask(myTaskSlot);
        rc.setIndicatorString(0, "slot:" + myTaskSlot + " task:initial");

        // This is NOT the inner loop.
        while (true) {
            getTaskFromHQ();
            rc.setIndicatorString(0, "slot:" + myTaskSlot + " task:" + currentTask.taskNum);

            // Order code is which task to pursue.
            int orderCode = currentTask.taskNum;

            switch (orderCode) {
                case (Task.MINERFACTORY):
                case (Task.SUPPLYDEPOT):
                case (Task.BARRACKS):
                case (Task.TANKFACTORY):
                case (Task.HELIPAD):
                case (Task.AEROSPACELAB):
                case (Task.TECHNOLOGYINSTITUTE):
                case (Task.TRAININGFIELD):
                    buildStructureMission();
                    break;
                case (Task.RESUPPLY_STRUCTURE):
                    resupplyMission();
                    break;
                case (Task.MINE):
                    while (true) {
                        callForHelp();
                        if (rc.getSupplyLevel() < LOW_SUPPLY) break;

                        if (rc.isCoreReady()) mine();
                        if (rc.isCoreReady()) wander();

                        rc.yield();
                    }
                    break;
                case (Task.NONE):
                    break;
                default:
                    System.err.println("ERROR: BEAVER sent on invalid mission ("+orderCode+"), please debug");
            }

            // Finished task.
            rc.setIndicatorString(1, "finished task");
            goToHQ();
            rc.setIndicatorString(1, "returned to hq");
            dumpSuppliesToHQ();
        }
    }

    private void buildStructureMission() {
        while (true) {
            callForHelp();

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
        int amt = Strategy.TANKFACTORY_RESUPPLY_AMT;

        while (true) {
            callForHelp();

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
            }

            rc.yield();
        }

    }

    private boolean buildThenSupplyForCode(int orderCode) {
        switch (orderCode) {
            case Task.MINERFACTORY:
                return buildThenSupply(MINERFACTORY);
            case Task.SUPPLYDEPOT:
                return buildThenSupply(SUPPLYDEPOT);
            case Task.BARRACKS:
                return buildThenSupply(BARRACKS);
            case Task.TANKFACTORY:
                return buildThenSupply(TANKFACTORY);
            case Task.HELIPAD:
                return buildThenSupply(HELIPAD);
            case Task.AEROSPACELAB:
                return buildThenSupply(AEROSPACELAB);
            case Task.TECHNOLOGYINSTITUTE:
                return buildThenSupply(TECHNOLOGYINSTITUTE);
            case Task.TRAININGFIELD:
                return buildThenSupply(TRAININGFIELD);
            default:
                System.err.println("ERROR: invalid building code " + orderCode);
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
    private boolean buildThenSupply(RobotType rob) {
        Direction dir = randomDirection();
        if (rc.canBuild(dir, rob)) {
            try {
                rc.build(dir, rob);
                MapLocation buildLoc = rc.getLocation().add(dir);
                rc.yield(); // Wait one turn for the building to spawn.
                rc.transferSupplies(Strategy.initialSupply(rob), buildLoc);
                waitForBuildCompletion(buildLoc);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // Wait (blocking) for a structure to finish being built by this robot.
    // If the building dies, that counts too.
    private void waitForBuildCompletion(MapLocation loc) {
        RobotInfo ri = null;
        do {
            callForHelp();

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
            callForHelp();

            rf.beavertasks.requestNewTask(myTaskSlot);
            // yield so the request propogates, otherwise we might see our old task.
            // and so that we don't request a task while having an assigned task.
            rc.yield();
            currentTask = rf.beavertasks.getTask(myTaskSlot);

            // Safety to ignore some corrupt tasks.
            if (currentTask != null && currentTask.taskNum == Task.RESUPPLY_STRUCTURE && currentTask.loc == null) {
                System.err.println("ERROR: resupply structure mission received with null location");
                currentTask = null;
            }
        } while (currentTask == null);

        rc.setIndicatorString(1, "received task");
    }

}
