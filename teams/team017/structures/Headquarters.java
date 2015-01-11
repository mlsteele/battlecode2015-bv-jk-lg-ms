package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;

import java.lang.System;
import java.util.*;

public class Headquarters extends Structure {

    private static final int MAX_BEAVER = 10;
    private static final int UPDATE_UNIT_COUNT_TIME = 10;
    int[] unitsOnField = new int[NUM_ROBOT_TYPES];

    private boolean beaver_mining_spawned = false;
    private boolean beaver_barracks_spawned = false;

    private MapLocation targetTower;
    private MapLocation earlyRallyLocation;

    private Hashtable<Integer, Integer> assignedBeaverTaskSlots = new Hashtable<Integer, Integer>();
    private Queue<Task> taskQueue = new LinkedList<Task>();

    Headquarters(RobotController rc) { super(rc); }

    @Override
    public void run() {

        earlyRallyLocation = avgLocations(0.35, rc.senseHQLocation(), rc.senseEnemyHQLocation());

        // Start by rallying at the closest tower
        MapLocation homeTower = closestTowerTo(rc.getLocation());
        rf.writeRally(Strategy.RALLY_GROUP_1, homeTower);
        rf.writeRally(Strategy.RALLY_GROUP_2, homeTower);

        taskQueue.add(new Task(TASK_MINERFACTORY));
        taskQueue.add(new Task(TASK_SUPPLYDEPOT));
        taskQueue.add(new Task(TASK_SUPPLYDEPOT));
        taskQueue.add(new Task(TASK_BARRACKS));
        taskQueue.add(new Task(TASK_TANKFACTORY));
        taskQueue.add(new Task(TASK_TANKFACTORY));
        taskQueue.add(new Task(TASK_TANKFACTORY));

        // Spawn the initial beavers
        // TODO(miles): Interlace this into the main loop so the HQ can multitask.
        for (int i = 0; i < BEAVER_POOL_SIZE; i++) {
            while (true) {
                if (rc.isCoreReady()) {
                    if (spawnBeaverWithTask(TASK_NONE, null))
                        break;
                }
            }
        }

        // set the max number of mining units
        rf.requestXUnits(RobotType.MINER, 20);

        while (true) {
            Analyze.sampleTeamOre(rc);

            shootBaddies();

            strategyUpdate();

            taskUpkeep();

            // Send out resupply beavers.
            // TODO(miles): did I break this?
            // TODO(miles): use existing beavers for resupply tasks.
            // TODO(miles): what if there is no one in getResupplyLocation()?
            if(rc.isCoreReady() && taskQueue.isEmpty()) {
                if (rf.resupplyFromTankFactoryRequested()) {
                    if (spawnBeaverWithTask(TASK_RESUPPLY_TANKFACTORY, rf.getResupplyLocation())) {
                        rf.clearResupplyRequest();
                    }
                }
            }

            // Resupply miners that have run out and returned.
            resupplyNearby(null, MINER, Strategy.MINER_LOW_SUPPLY, Strategy.MINER_RESUPPLY_FROM_HQ);

            rc.yield();
        }
    }

    private void strategyUpdate() {

        MapLocation oldTargetTower = targetTower;

        updateTargetTower();

        // Updates the unit count. Happens every UPDATE_UNIT_COUNT_TIME mod times
        if (Clock.getRoundNum() % UPDATE_UNIT_COUNT_TIME == 0) {
            int[] cheese = updateUnitCount();
            // rc.setIndicatorString(0, Arrays.toString(cheese));
        }

        // Rally at 0.35 of the way there.
        if (Math.abs(Clock.getRoundNum() - Strategy.EARLY_RALLY_GROUP_1) <= 1) {
            rf.writeRally(Strategy.RALLY_GROUP_1, earlyRallyLocation);
            rc.setIndicatorString(1, "Early rally at " + earlyRallyLocation);
        }

        // Rally at an enemy tower, move up 2nd group
        if (Math.abs(Clock.getRoundNum() - Strategy.ATTACK_GROUP_1) <= 1) {
            rf.writeRally(Strategy.RALLY_GROUP_1, targetTower);
            rf.writeRally(Strategy.RALLY_GROUP_2, earlyRallyLocation);
            rc.setIndicatorString(1, "Group 1 moves forward");
        }

        // Everyone should attack now
        if (Math.abs(Clock.getRoundNum() - ATTACK_GROUP_2) <= 1) {
            rf.writeRally(Strategy.RALLY_GROUP_2, targetTower);
            rc.setIndicatorString(1, "Everyone attacks");
        }

        // If we have a new target tower, update rally points of attackers
        if (targetTower != oldTargetTower) {
            if (Clock.getRoundNum() > Strategy.ATTACK_GROUP_1 + 1) {
                rf.writeRally(Strategy.RALLY_GROUP_1, targetTower);
            }
            if (Clock.getRoundNum() > Strategy.ATTACK_GROUP_2 + 1) {
                rf.writeRally(Strategy.RALLY_GROUP_2, targetTower);
            }
        }
    }

    private void updateTargetTower() {
        // Target the tower closest to group 1
        MapLocation rallyPoint = rf.getRally(Strategy.RALLY_GROUP_1);
        if (rallyPoint == null) rallyPoint = earlyRallyLocation;
        targetTower = closestEnemyTowerTo(rallyPoint);
        rc.setIndicatorDot(targetTower, 0, 255, 0);
    }


    private double ourTankHealth() {
        double sum = 0;
        for (RobotInfo r : rc.senseNearbyRobots()) {
            if (r.team == rc.getTeam() && r.type == TANK) {
                sum += r.health;
            }
        }
        return sum;
    }

    private void taskUpkeep() {
        // check if there are any tasks
        if (taskQueue.isEmpty()) return;
        Task nextTask = taskQueue.peek();

        if (rc.getSupplyLevel() < Strategy.taskSupply(nextTask.taskNum)) {
            return;
        }

        int taskSlot;

        // check if anyone wants tasks
        taskSlot = rf.assignTaskToNextFree(nextTask);
        if (taskSlot < 0) {
            // no one can get tasks so leave it in the queue.
            return;
        }

        // we have given the beaver the task, lets transfer the supplies
        int robotID = assignedBeaverTaskSlots.get((Integer) taskSlot);
        // TODO(miles): block the hq? :( What if the beaver dies?
        // Block until beaver gets supply
        while (!supplyToID(null, robotID, Strategy.taskSupply(nextTask.taskNum))) continue;
        // we have given the supplies, we can remove the task now
        taskQueue.remove();
        System.out.println("dispatched task ("+taskQueue.size()+" tasks remaining)");
    }

    // Assumes supply level desired
    private boolean spawnBeaverWithTask(int task, MapLocation loc) {
        if(rc.getSupplyLevel() < Strategy.taskSupply(task)) return false;

        Direction dir = spawn(BEAVER); // spawn the beaver
        if (dir == null) return false;

        int beaverTaskSlot = rf.assignBeaverTaskSlot(); // Assign a new beaver task slot
        if (beaverTaskSlot < 0) {
            return false; // someone hasnt claimed their task, shame on them
        }
        rf.setTask(new Task(task, loc), beaverTaskSlot); // give the beaver a task

        // Yield so that the spawned beaver exists.
        rc.yield();

        RobotInfo rob;
        try {
            rob = rc.senseRobotAtLocation(rc.getLocation().add(dir)); // gets its info
        } catch (GameActionException e) {
            e.printStackTrace();
            return false;
        }

        RobotInfo[] candidates = {rob};
        assignedBeaverTaskSlots.put(beaverTaskSlot, rob.ID);
        return supplyToID(candidates, rob.ID, Strategy.taskSupply(task));
    }


    private MapLocation avgLocations(MapLocation a, MapLocation b) {
        return new MapLocation((a.x + b.x) / 2, (a.y + b.y) / 2);
    }

    // mixFactor is a number [0.0,1.0] that describes the weighted average.
    // mixFactor:0 will return a, mixFactor:0.5 will return the average.
    private MapLocation avgLocations(double mixFactor, MapLocation a, MapLocation b) {
        double ma = 1.0 - mixFactor;
        double mb = mixFactor;
        return new MapLocation((int)(ma*a.x + mb*b.x),
                               (int)(ma*a.y + mb*b.y));
    }

    private boolean supplyBeaver(int supplyAmount) {
        if (rc.getSupplyLevel() < supplyAmount)
            return false;

        RobotInfo[] candidates = rc.senseNearbyRobots(
                    GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                    rc.getTeam());

        for (RobotInfo r : candidates) {
            // Only send to the correct type of bot.
            if (r.type != RobotType.BEAVER) continue;
            if (r.supplyLevel > 0) continue;

            try {
                rc.transferSupplies(supplyAmount, r.location);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }

        }

        return false;
    }
}
