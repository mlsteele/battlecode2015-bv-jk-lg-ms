package team017;

import battlecode.common.*;
import battlecode.common.Clock;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;

import java.lang.System;
import java.util.*;

public class Headquarters extends Structure {

    private static final int MAX_BEAVER = 10;
    private static final int UPDATE_UNIT_COUNT_TIME = 10;
    private int[] unitCounts;

    private MapLocation targetTower;
    private MapLocation earlyRallyLocation;

    private Hashtable<Integer, Integer> assignedBeaverTaskSlots = new Hashtable<Integer, Integer>();
    private Queue<Task> taskQueue = new LinkedList<Task>();

    private int desiredMiners = 30;
    private int desiredTankFactories = 3;
    private boolean additionalSupplyDepots = true;

    // Amt of time HQ will wait for a building before requesting again
    private static final int waitTimeForSpawn(RobotType rtype) {
        return 15 + rtype.buildTurns;
    }

    // How long until we should ask again for a building?
    private int lastBarracksRequestTime = 0;
    private int lastFactoryRequestTime = 0;

    Headquarters(RobotController rc) { super(rc); }

    @Override
    public void run() {

        earlyRallyLocation = avgLocations(0.35, rc.senseHQLocation(), rc.senseEnemyHQLocation());

        // Start by rallying at the closest tower
        MapLocation homeTower = closestTowerTo(rc.getLocation());
        rf.rallypoints.set(Strategy.RALLY_GROUP_1, homeTower);
        rf.rallypoints.set(Strategy.RALLY_GROUP_2, homeTower);

        taskQueue.add(new Task(Task.MINERFACTORY));
        taskQueue.add(new Task(Task.HELIPAD));
        taskQueue.add(new Task(Task.SUPPLYDEPOT));
        taskQueue.add(new Task(Task.SUPPLYDEPOT));
        taskQueue.add(new Task(Task.BARRACKS));

        for (int i=0; i<desiredTankFactories; i++)
            taskQueue.add(new Task(Task.TANKFACTORY));

        rf.xunits.set(MINER, desiredMiners);
        rf.xunits.set(DRONE, DRONE_HARRASS_N);

        while (true) {
            if (Analyze.ON) Analyze.sample("team_ore", rc.getTeamOre());
            if (Analyze.ON) Analyze.sample("hq_supply", rc.getSupplyLevel());

            shootBaddies();

            strategyUpdate();

            if (rc.isCoreReady() && unitCounts[BEAVER.ordinal()] < BEAVER_POOL_SIZE) {
                // Hardcode limit for now to only build 1 beaver early on.
                // This gets us our first building faster.
                if (unitCounts[BEAVER.ordinal()] == 0 || Clock.getRoundNum() > 100) {
                    spawnBeaver();
                }
            }

            if (Clock.getRoundNum() > 600)
                maintainDesiredTankFactories();

            if(rc.getTeamOre() > 1000 && additionalSupplyDepots) {
                desiredTankFactories = 5;
                taskQueue.add(new Task(Task.SUPPLYDEPOT));
                taskQueue.add(new Task(Task.SUPPLYDEPOT));
                taskQueue.add(new Task(Task.SUPPLYDEPOT));
                additionalSupplyDepots = false;
            }

            taskUpkeep();

            // Collect resupply tasks.
            if (rf.resupply.requested()) {
                taskQueue.add(new Task(Task.RESUPPLY_TANKFACTORY, rf.resupply.getLocation()));
                rf.resupply.clearRequest();
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
            unitCounts = getUnitCounts();
            if (Analyze.ON) Analyze.sample("beavers", unitCounts[BEAVER.ordinal()]);
            if (Analyze.ON) Analyze.sample("miners", unitCounts[MINER.ordinal()]);
            if (Analyze.ON) Analyze.sample("tanks", unitCounts[TANK.ordinal()]);
            if (Analyze.ON) Analyze.sample("tfs", unitCounts[TANKFACTORY.ordinal()]);
            if (Analyze.ON) Analyze.sample("drones", unitCounts[DRONE.ordinal()]);
            if (Analyze.ON) Analyze.sample("dsupply", supplyGainRate());
        }

        // Rally at 0.35 of the way there.
        if (Math.abs(Clock.getRoundNum() - Strategy.EARLY_RALLY_GROUP_1) <= 1) {
            rf.rallypoints.set(Strategy.RALLY_GROUP_1, earlyRallyLocation);
            rc.setIndicatorString(1, "Early rally at " + earlyRallyLocation);
        }

        // Rally at an enemy tower, move up 2nd group
        if (Math.abs(Clock.getRoundNum() - Strategy.ATTACK_GROUP_1) <= 1) {
            rf.rallypoints.set(Strategy.RALLY_GROUP_1, targetTower);
            rf.rallypoints.set(Strategy.RALLY_GROUP_2, earlyRallyLocation);
            rc.setIndicatorString(1, "Group 1 moves forward");
        }

        // Everyone should attack now
        if (Math.abs(Clock.getRoundNum() - ATTACK_GROUP_2) <= 1) {
            rf.rallypoints.set(Strategy.RALLY_GROUP_2, targetTower);
            rc.setIndicatorString(1, "Everyone attacks");
        }

        // If we have a new target tower, update rally points of attackers
        if (targetTower != oldTargetTower) {
            if (Clock.getRoundNum() > Strategy.ATTACK_GROUP_1 + 1) {
                rf.rallypoints.set(Strategy.RALLY_GROUP_1, targetTower);
            }
            if (Clock.getRoundNum() > Strategy.ATTACK_GROUP_2 + 1) {
                rf.rallypoints.set(Strategy.RALLY_GROUP_2, targetTower);
            }
        }
    }

    private void updateTargetTower() {
        // Target the tower closest to group 1
        MapLocation rallyPoint = rf.rallypoints.get(RALLY_GROUP_1);
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
        taskSlot = rf.beavertasks.assignTaskToNextFree(nextTask);
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
        //System.out.println("dispatched task ("+taskQueue.size()+" tasks remaining)");
    }

    private boolean spawnBeaver() {
        // try to spawn the beaver.
        Direction dir = spawn(BEAVER);
        if (dir == null) return false;
        if (Analyze.ON) Analyze.sample("hq_spawn_beaver", 1);

        int beaverTaskSlot = rf.beavertasks.assignBeaverTaskSlot(); // Assign a new beaver task slot
        if (beaverTaskSlot < 0) {
            // someone hasnt claimed their task, shame on them
            System.err.println("ERROR: unclaimed beaver task slot. This should not happen.");
            return false;
        }

        // give the beaver an empty task.
        rf.beavertasks.setTask(beaverTaskSlot, new Task(Task.NONE));

        // yield so that the spawned beaver exists.
        rc.yield();

        RobotInfo rob;
        try {
            rob = rc.senseRobotAtLocation(rc.getLocation().add(dir)); // gets its info
            if (rob == null) {
                System.err.println("ERROR: HQ could not sense new beaver. Dodging NPE, but please fix this.");
                return false;
            }
        } catch (GameActionException e) {
            e.printStackTrace();
            return false;
        }

        assignedBeaverTaskSlots.put(beaverTaskSlot, rob.ID);
        return true;
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

    // The boolean return here is sketchy. returns true if it "did something"
    private boolean maintainDesiredTankFactories() {
        unitCounts = getUnitCounts();
        int numQueuedFactories = Collections.frequency(taskQueue, new Task(Task.TANKFACTORY));
        int numQueuedBarracks = Collections.frequency(taskQueue, new Task(Task.BARRACKS));
        int neededFactories = desiredTankFactories - unitCounts[TANKFACTORY.ordinal()] - numQueuedFactories;

        // if we don't have (or have queued) enough factories
        if(neededFactories > 0) {
            // System.out.println("We need " + neededFactories + " factories");
            // if we don't have (or have queued) a barracks
            if(unitCounts[BARRACKS.ordinal()] + numQueuedBarracks < 1) {
                // If we've waited enough since we last tried for a barracks
                if(lastBarracksRequestTime + waitTimeForSpawn(BARRACKS) <= Clock.getRoundNum()) {
                    // queue a new barracks
                    taskQueue.add(new Task(Task.BARRACKS));
                    lastBarracksRequestTime = Clock.getRoundNum();
                    return true;
                }
            } else { // Barracks is all set.
                // If we've waited enough since we last tried for a tank factory
                if(lastFactoryRequestTime + waitTimeForSpawn(TANKFACTORY) <= Clock.getRoundNum()) {
                    // queue a new tank factory
                    taskQueue.add(new Task(Task.TANKFACTORY));
                    lastFactoryRequestTime = Clock.getRoundNum();
                    return true;
                }
            }
        }
        return false;
    }

    // Rate of supply gain.
    // Requires that unitCounts be up to date
    // with the number of supply depots.
    private double supplyGainRate() {
        // Formula copied from the engine.
        // Be sure to be on the lookout for changes.
        return GameConstants.SUPPLY_GEN_BASE *
            (GameConstants.SUPPLY_GEN_MULTIPLIER +
                Math.pow(
                    unitCounts[SUPPLYDEPOT.ordinal()],
                    GameConstants.SUPPLY_GEN_EXPONENT));
    }
}
