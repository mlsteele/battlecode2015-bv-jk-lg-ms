package team017.units;

import static battlecode.common.RobotType.*;
import team017.*;
import battlecode.common.*;

public class Beaver extends Unit {
    public Beaver(RobotController rc) { super(rc); }

    private static final int LOW_SUPPLY = 50;

    // Require this distance free space around buildings
    private static final int BUILDING_PADDING = 2;
    private static final int BUILDING_PADDING_SENSE = 10;

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
                case (Task.HANDWASHSTATION):
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
        rc.setIndicatorString(1, "finding place to build");

        // Reserve ore for the structure.
        rf.beavertasks.incReservedOre(currentTask.requiredOre());

        // Travel in a spiral-kinda-thing.
        Direction startAxis = randomDirection();
        Direction axis = startAxis;
        int radius = 2;
        while (true) {
            callForHelp();

            if (!rc.isCoreReady()) {
                rc.yield();
                continue;
            }

            if (!rc.hasBuildRequirements(currentTask.structureType())) {
                rc.yield();
                continue;
            }

            if (buildStructure(currentTask.structureType())) {
                // Structure successfully built, clear reserved ore.
                rf.beavertasks.lowerReservedOre(currentTask.requiredOre());
                return;
            }

            // Move
            MapLocation target = hqLoc.add(axis, radius);
            Bugging.setParams(target, 4, false);
            Bugging.move();

            // Safety.
            if (radius > 15) {
                radius = 2;
            }

            // Advance spiral.
            if (target.distanceSquaredTo(rc.getLocation()) <= 4) {
                axis = axis.rotateLeft();
                if (axis.equals(startAxis))
                    radius += 1;
            }

            rc.yield();
        }
    }

    private void resupplyMission() {
        Bugging.setParams(currentTask.loc, 0, false);

        while (true) {
            callForHelp();

            if (rc.isCoreReady()) {
                if (rc.getLocation().distanceSquaredTo(currentTask.loc) > GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
                    // Too far away.
                    Bugging.move();
                    rc.yield();
                } else {
                    // Close enough to transfer supplies.
                    try {
                        rc.transferSupplies(currentTask.amount, currentTask.loc);
                        rc.yield();
                    } catch (GameActionException e) {
                        e.printStackTrace();
                    }
                    // Declare task done no matter what.
                    return;
                }
            }

            rc.yield();
        }

    }

    // Attempt to build a structure anywhere adjacent.
    // Assumes CoreReady
    private boolean buildStructure(RobotType rob) {
        Direction dir = randomDirection();
        RobotInfo[] nearby = rc.senseNearbyRobots(BUILDING_PADDING_SENSE);

        // Try building in each direction.
        for (int i = 0; i < 8; i++) {
            if (rc.canBuild(dir, rob) && isClearToBuild(nearby, rc.getLocation().add(dir))) {
                try {
                    rc.build(dir, rob);
                    waitForStructureBuilt();
                    return true;
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
            dir = dir.rotateLeft();
        }
        return false;
    }

    // Wait until a structure is finished building.
    // Actually just wait for CoreDelay to come down.
    private void waitForStructureBuilt() {
        while (!rc.isCoreReady()) {
            callForHelp();
            rc.yield();
        }
    }

    // `nearby` is all nearby robots to factor into clearness.
    private boolean isClearToBuild(RobotInfo[] nearby, MapLocation place) {
        for (RobotInfo r : nearby) {
            if (place.distanceSquaredTo(r.location) > BUILDING_PADDING)
                continue;
            if (r.type.isBuilding || r.team == rc.getTeam().opponent())
                return false;
        }
        return true;
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
