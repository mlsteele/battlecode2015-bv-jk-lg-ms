package droneHarrass.structures;

import static battlecode.common.RobotType.*;
import static droneHarrass.Strategy.*;

import java.util.*;

import droneHarrass.*;
import battlecode.common.*;

public class Headquarters extends Structure {

    private static final int MAX_BEAVER = 10;
    private static final int UPDATE_UNIT_COUNT_TIME = 10;
    private int[] unitCounts;

    private MapLocation targetTower;
    private MapLocation earlyRallyLocation;

    // Mapping from taskSlot -> beaverID
    private Hashtable<Integer, Integer> beaverMap = new Hashtable<Integer, Integer>();
    private Queue<Task> taskQueue = new LinkedList<Task>();

    private int desiredMiners = 30;
    private int desiredTankFactories = 3;
    private boolean additionalSupplyDepots = true;
    private double desiredTankDroneRatio = 3;

    // Amt of time HQ will wait for a building before requesting again
    private static final int waitTimeForSpawn(RobotType rtype) {
        return 15 + rtype.buildTurns;
    }

    // How long until we should ask again for a building?
    private int lastBarracksRequestTime = 0;
    private int lastFactoryRequestTime = 0;

    private float miningRate = 0;

    public Headquarters(RobotController rc) { super(rc); }

    @Override
    public void run() {
        earlyRallyLocation = avgLocations(0.35, rc.senseHQLocation(), rc.senseEnemyHQLocation());

        // Start by rallying at the closest tower
        MapLocation homeTower = closestTowerTo(rc.getLocation());
        rf.rallypoints.set(Strategy.RALLY_GROUP_1, homeTower);
        rf.rallypoints.set(Strategy.RALLY_GROUP_2, homeTower);

        taskQueue.add(new Task(Task.HELIPAD));
        taskQueue.add(new Task(Task.HELIPAD));
        taskQueue.add(new Task(Task.MINERFACTORY));
        taskQueue.add(new Task(Task.SUPPLYDEPOT));
        taskQueue.add(new Task(Task.SUPPLYDEPOT));
        //taskQueue.add(new Task(Task.BARRACKS));

        //for (int i=0; i<desiredTankFactories; i++)
        //    taskQueue.add(new Task(Task.TANKFACTORY));

        //taskQueue.add(new Task(Task.TECHNOLOGYINSTITUTE));
        //taskQueue.add(new Task(Task.TRAININGFIELD));
        rf.xunits.set(MINER, desiredMiners);
        rf.xunits.set(DRONE, DRONE_HARRASS_N);

        while (true) {
            callForHelp();

            miningRate = rf.miningrate.getAveraged();
            rf.miningrate.set(0);

            if (Analyze.ON) Analyze.sample("team_ore", rc.getTeamOre());
            if (Analyze.ON) Analyze.sample("hq_supply", rc.getSupplyLevel());
            if (Analyze.ON) Analyze.sample("miningrate", miningRate);

            rf.beavertasks.discoverBeaverTaskSlot(beaverMap);

            shootBaddies();

            strategyUpdate();

            if (rc.isCoreReady() && unitCounts[BEAVER.ordinal()] < BEAVER_POOL_SIZE) {
                // Hardcode limit for now to only build 1 beaver early on.
                // This gets us our first building faster.
                if (unitCounts[BEAVER.ordinal()] == 0 || Clock.getRoundNum() > 100) {
                    spawnBeaver();
                }
            }

            if (Clock.getRoundNum() > 600 && Clock.getRoundNum() % UPDATE_UNIT_COUNT_TIME == 0) {
                maintainDesiredTankFactories();
                //maintainTankDroneRatio(desiredTankDroneRatio);
            }

            if(rc.getTeamOre() > 1000 && additionalSupplyDepots) {
                desiredTankFactories = 5;
                taskQueue.add(new Task(Task.SUPPLYDEPOT));
                taskQueue.add(new Task(Task.SUPPLYDEPOT));
                taskQueue.add(new Task(Task.SUPPLYDEPOT));
                additionalSupplyDepots = false;
            }

            taskUpkeep();

            // Collect resupply tasks.
            if (rf.resupply.isRequested()) {
                taskQueue.add(new Task(Task.RESUPPLY_STRUCTURE, rf.resupply.getLocation(), rf.resupply.getAmount()));
                rf.resupply.clearRequest();
            }

            resupplyNearby(null, MINER, Strategy.MINER_LOW_SUPPLY, Strategy.MINER_RESUPPLY_FROM_HQ);
            resupplyNearby(null, DRONE, Strategy.initialSupply(DRONE), Strategy.initialSupply(DRONE));

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

    // Consume the task queue, assigning tasks to beavers.
    private void taskUpkeep() {
        // Tasks waiting?
        if (taskQueue.isEmpty()) return;
        Task nextTask = taskQueue.peek();

        // Task is ready to be assigned?
        if (rc.getSupplyLevel() < Strategy.taskSupply(nextTask)) {
            return;
        }

        // Beaver waiting for ask?
        int taskSlot = rf.beavertasks.assignTaskToNextFree(nextTask);
        if (taskSlot < 0) {
            // No one waiting for tasks, leave it in the queue.
            return;
        }

        // Beaver has been assigned task.
        Integer robotID_I = beaverMap.get((Integer) taskSlot);
        if (robotID_I == null) {
            System.err.println("ERROR: beaver id for task slot not in map.");
            return;
        }
        int robotID = robotID_I;

        // Try to supply beaver.
        if (!supplyToID(null, robotID, Strategy.taskSupply(nextTask))) {
            // Failed to supply beaver. It might have died. Abort the task assignment.
            System.out.println("WARNING: Failed to supply beaver ["+robotID+"] after assigning task ["+nextTask+"].");
            rf.beavertasks.setTask(taskSlot, new Task(Task.NONE));
            return;
        }

        // We have given the supplies, we can remove the task now
        taskQueue.remove();
    }

    // Try to spawn the beaver.
    private boolean spawnBeaver() {
        Direction dir = spawn(BEAVER);
        if (dir == null) return false;
        if (Analyze.ON) Analyze.sample("hq_spawn_beaver", 1);
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

    private void maintainTankDroneRatio(double desiredRatio) {
        if(shouldRationOre()) {
            int numTanks = unitCounts[TANK.ordinal()];
            int numDrones = unitCounts[DRONE.ordinal()];
            double wiggleRoom = 0.1;
            double existingRatio;
            if (numDrones == 0) existingRatio = 999; // avoid divide by zero
            else existingRatio = (numTanks*1.0)/numDrones;
            System.out.println("Existing tank/drone ratio is " + existingRatio);
            if (existingRatio - wiggleRoom > desiredRatio) {
                // Too many tanks
                rf.limitproduction.stopBuilding(TANK);
                rf.limitproduction.resumeBuilding(DRONE);
            } else if (existingRatio + wiggleRoom < desiredRatio) {
                // Too many drones.
                rf.limitproduction.stopBuilding(DRONE);
                rf.limitproduction.resumeBuilding(TANK);
            } else {
                // Good. build both
                rf.limitproduction.resumeBuilding(TANK);
                rf.limitproduction.resumeBuilding(DRONE);
            }
        }
    }

    private boolean shouldRationOre() {
        final int PLENTY_OF_ORE = desiredTankFactories * TANK.oreCost + 4 * DRONE.oreCost + 200;
        System.out.println(PLENTY_OF_ORE + " is plenty of ore.");
        return rc.getTeamOre() < PLENTY_OF_ORE;
    }
}
