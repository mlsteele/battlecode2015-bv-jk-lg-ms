package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import static battlecode.common.Direction.*;

import java.util.*;

public class Miner extends Unit {
    Miner(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a Miner");

        waitForSupplies();

        while (true) {
            if (rc.isCoreReady()) {
                boolean miner_low_supply = rc.getSupplyLevel() > Strategy.MINER_LOW_SUPPLY;
                boolean team_low_ore     = rc.getSupplyLevel() > Strategy.TEAM_LOW_ORE;
                if (miner_low_supply && team_low_ore) {
                    goToHQ();
                } else {
                    runAway();
                    pursueMining();
                }
            }

            rc.yield();
        }
    }

    // If an enemy robot is nearby, go the other direction
    private void runAway() {
        int range = rc.getType().sensorRadiusSquared;
        Team enemy = rc.getTeam().opponent();

        RobotInfo[] enemies = rc.senseNearbyRobots(range, enemy);

        // Move away from first bad guy.
        // TODO you could move smarter than this
        if (enemies.length > 0 && rc.isCoreReady())
            moveToward(enemies[0].location.directionTo(rc.getLocation()));

    }

    private void pursueMining() {
        if (rc.senseOre(rc.getLocation()) > 0) {
            mineHere();
        } else {
            forward = optimalOreDirection();
            moveForward();
        }
    }

    // Returns true if mining occurred.
    // Assumes there is ore
    private boolean mineHere() {
        try {
            rc.mine();
            return true;
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Which direction's adjacent location has the most ore.
    public Direction optimalOreDirection() {
        Direction[] possibleDirs = {NORTH, NORTH_EAST, EAST, SOUTH_EAST,
                                    SOUTH, SOUTH_WEST, WEST, NORTH_WEST};

        MapLocation curLocation = rc.getLocation();
        double bestOre = 0;
        Direction bestDirection = NORTH;

        Direction d;
        for (int i = rand.nextInt(8); i < 8; i++) {
            d = possibleDirs[i];
            double ore = rc.senseOre(curLocation.add(d));
            if (ore > bestOre && rc.canMove(d)) {
                bestOre = ore;
                bestDirection = d;
            }
        }

        return bestDirection;
    }
}
