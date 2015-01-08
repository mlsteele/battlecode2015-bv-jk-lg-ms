package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;

import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
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

    Headquarters(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am an HQ");

        // Tell all soldiers to rally at our first tower.
        rf.writeRally1(rc.senseTowerLocations()[0]);
        rc.setIndicatorString(1, "set rally1 to " + rf.getRally1());

        int missionIndex = 0;

        while (true) {
            shootBaddies();

            strategyUpdate();

            // Spawn a beaver.
            if (rc.isCoreReady()) maybeSpawnBeaver();

            switch (missionIndex) {
                case 0:
                    if (supplyForMinerFactory()) missionIndex++;
                    break;
                case 1:
                    if (supplyForHelipad()) missionIndex++;
                    break;
                case 2:
                    if (supplyForBarracks()) missionIndex++;
                    break;
                case 3:
                    if (supplyForTankFactory()) missionIndex++;
                    break;
                default: break;
            }

            rc.yield();
        }
    }

    private void strategyUpdate() {
        // Updates the unit count. Happens every UPDATE_UNIT_COUNT_TIME mod times
        if (Clock.getRoundNum() % UPDATE_UNIT_COUNT_TIME == 0) {
            int[] cheese = updateUnitCount();
            rc.setIndicatorString(0, Arrays.toString(cheese));
        }

        if (Math.abs(Clock.getRoundNum() - TIME_TO_ATTACK1) <= 1) {
            // Rally at half point.
            rf.writeRally1(avgLocations(rc.senseEnemyHQLocation(), rc.senseHQLocation()));
            rc.setIndicatorString(1, "set rally1 to " + rf.getRally1());
        }

        if (Math.abs(Clock.getRoundNum() - TIME_TO_ATTACK2) <= 1) {
            // Rally at the enemy HQ
            rf.writeRally1(rc.senseEnemyHQLocation());
            rc.setIndicatorString(1, "set rally1 to " + rf.getRally1());
        }

    }

    private Direction smartSpawn(RobotType robot) {
        if (robot == RobotType.BEAVER) {
            try {
                int beaverJobSlot = rf.assignBeaverJobSlot();
                if (beaverJobSlot < 0) return null;
                rc.setIndicatorString(2, "Creating beaver : slot = " + beaverJobSlot);
                rf.setJob(spawned_beavers, beaverJobSlot);
                return spawn(BEAVER);
            } catch (GameActionException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return spawn(robot);
        }
    }

    private void maybeSpawnBeaver() {
        if (rc.getSupplyLevel() >= Strategy.initialSupply(BEAVER) && spawned_beavers < MAX_BEAVER) {
            if (smartSpawn(BEAVER) != null) {
                spawned_beavers++;
            }
        }
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
                System.out.println("HQ sending " + supplyAmount + " supplies");
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }

        }

        return false;
    }
}
