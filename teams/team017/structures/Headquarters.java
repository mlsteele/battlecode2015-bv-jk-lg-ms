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
    private static final int NUM_OF_UNIT_TYPES = RobotType.values().length;
    int[] unitsOnField = new int[NUM_OF_UNIT_TYPES];

    private int spawned_beavers = 0;
    private boolean beaver_mining_spawned = false;
    private boolean beaver_barracks_spawned = false;

    private Hashtable<Integer, Integer> assignedBeaverTaskSlots = new Hashtable<Integer, Integer>();
    private Queue<Task> taskQueue = new LinkedList<Task>();

    Headquarters(RobotController rc) { super(rc); }

    @Override
    public void run() {
        // Tell all soldiers to rally at our first tower.
        rf.writeRally(RALLY_ARMY, rc.senseTowerLocations()[0]);

        int missionIndex = 0;

        taskQueue.add(new Task(Strategy.TASK_BARRACKS));
        taskQueue.add(new Task(Strategy.TASK_BARRACKS));

        while (true) {
            shootBaddies();

            strategyUpdate();

            taskUpkeep();

            // Spawn a beaver.
            if(rc.isCoreReady()) {
                switch (missionIndex) {
                    case 0:
                    case 1:
                    case 2:
                        if (spawnBeaverWithStrategy(TASK_SUPPLYDEPOT, null)) missionIndex++;
                        break;
                    case 3:
                        if (spawnBeaverWithStrategy(TASK_MINERFACTORY, null)) missionIndex++;
                        break;
                    case 4:
                        if (spawnBeaverWithStrategy(TASK_BARRACKS, null)) missionIndex++;
                        break;
                    case 5:
                    case 6:
                    case 7:
                        if (spawnBeaverWithStrategy(TASK_TANKFACTORY, null)) missionIndex++;
                        break;
                    default:
                        if (rf.resupplyFromTankFactoryRequested()) {
                            if (spawnBeaverWithStrategy(TASK_RESUPPLY_TANKFACTORY, rf.getResupplyLocation())) {
                                rf.clearResupplyRequest();
                            }
                        }

                        break;
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
        Task nextTask = taskQueue.peek();
        if (nextTask == null) return;

        if (rc.getSupplyLevel() < Strategy.taskSupply(nextTask.taskNum)) {
            return;
        }

        int taskSlot;


        // check if anyone wants tasks
        taskSlot = rf.assignTaskToNextFree(nextTask);
        if (taskSlot < 0) {
            // no one can get tasks so add the task back to queue
            return;
        }


        // we have given the beaver the task, lets transfer the supplies
        int robotID = assignedBeaverTaskSlots.get((Integer) taskSlot);
        while (!supplyToID(null, robotID, Strategy.taskSupply(nextTask.taskNum))) continue;
        // we have given the supplies, we can remove the task now
        taskQueue.remove();
    }

    // Assumes supply level desired
    private boolean spawnBeaverWithStrategy(int task, MapLocation loc) {
        if(rc.getSupplyLevel() < Strategy.taskSupply(task)) return false;

        Direction dir = spawn(BEAVER); // spawn the beaver
        if (dir == null) return false;

        int beaverTaskSlot = rf.assignBeaverTaskSlot(); // Assign a new beaver task slot
        if (beaverTaskSlot < 0) {
            return false; // someone hasnt claimed their task, shame on them
        }
        rf.setTask(new Task(task, loc), beaverTaskSlot); // give the beaver a task

        rc.yield();

        RobotInfo rob;
        try {
            rob = rc.senseRobotAtLocation(rc.getLocation().add(dir)); // gets its info
        } catch (GameActionException e) {
            e.printStackTrace();
            return false;
        }

        int robotID = rob.ID; // get its id
        RobotInfo[] candidates = {rob};
        assignedBeaverTaskSlots.put(beaverTaskSlot, robotID);
        return supplyToID(candidates, robotID, Strategy.taskSupply(task));
    }

    private int[] updateUnitCount() {
        int[] unitsOnField = new int[NUM_OF_UNIT_TYPES];
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getLocation(), Integer.MAX_VALUE, rc.getTeam());
        for (RobotInfo rob : robots) {
            unitsOnField[rob.type.ordinal()]++;
        }
        return unitsOnField;
    }


    private MapLocation avgLocations(MapLocation a, MapLocation b) {
        return new MapLocation((a.x + b.x) / 2, (a.y + b.y) / 2);
    }

    private boolean supplyForMinerFactory() {
        if (rc.getTeamOre() < MINERFACTORY.oreCost) return false;
        return supplyBeaver(Strategy.initialSupply(BEAVER) + Strategy.initialSupply(MINERFACTORY) + Strategy.TASK_MINERFACTORY);
    }

    private boolean supplyForBarracks() {
        if (rc.getTeamOre() < BARRACKS.oreCost) return false;
        return supplyBeaver(Strategy.initialSupply(BEAVER) + Strategy.initialSupply(BARRACKS) + Strategy.TASK_BARRACKS);
    }

    private boolean supplyForTankFactory() {
        if (rc.getTeamOre() < TANKFACTORY.oreCost) return false;
        return supplyBeaver(Strategy.initialSupply(BEAVER) + Strategy.initialSupply(TANKFACTORY) + Strategy.TASK_TANKFACTORY);
    }

    private boolean supplyForHelipad() {
        if (rc.getTeamOre() < HELIPAD.oreCost) return false;
        return supplyBeaver(Strategy.initialSupply(BEAVER) + Strategy.initialSupply(HELIPAD) + Strategy.TASK_HELIPAD);
    }

    private boolean supplyForWander() {
        return supplyBeaver(Strategy.initialSupply(BEAVER) + Strategy.TASK_NONE);
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
