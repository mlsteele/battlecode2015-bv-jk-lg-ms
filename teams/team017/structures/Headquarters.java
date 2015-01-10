package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;

import java.lang.System;
import java.util.*;

public class Headquarters extends Structure {
    private static final int TIME_TO_ATTACK1 = 500;
    private static final int TIME_TO_ATTACK2 = 1000;
    private static final int MAX_BEAVER = 10;
    private static final int UPDATE_UNIT_COUNT_TIME = 10;
    int[] unitsOnField = new int[NUM_ROBOT_TYPES];

    private boolean beaver_mining_spawned = false;
    private boolean beaver_barracks_spawned = false;

    private Hashtable<Integer, Integer> assignedBeaverTaskSlots = new Hashtable<Integer, Integer>();
    private Queue<Task> taskQueue = new LinkedList<Task>();

    Headquarters(RobotController rc) { super(rc); }

    @Override
    public void run() {
        // Tell all soldiers to rally at our first tower.
        rf.writeRally(RALLY_ARMY, rc.senseTowerLocations()[0]);

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
        rf.requestXUnits(RobotType.MINER, 10);

        while (true) {
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
        // Updates the unit count. Happens every UPDATE_UNIT_COUNT_TIME mod times
        if (Clock.getRoundNum() % UPDATE_UNIT_COUNT_TIME == 0) {
            int[] cheese = updateUnitCount();
            // rc.setIndicatorString(0, Arrays.toString(cheese));
        }

        if (Math.abs(Clock.getRoundNum() - TIME_TO_ATTACK1) <= 1) {
            // Rally at half point.
            rf.writeRally(RALLY_ARMY, avgLocations(rc.senseEnemyHQLocation(), rc.senseHQLocation()));
        }

        if (Math.abs(Clock.getRoundNum() - TIME_TO_ATTACK2) <= 1) {
            // Rally at the enemy HQ
            rf.writeRally(RALLY_ARMY, rc.senseEnemyHQLocation());
        }
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
