package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import static battlecode.common.Direction.*;

import java.util.*;

public class Miner extends Unit {
    Miner(RobotController rc) { super(rc); }

    // Return to HQ to resupply if below this level.
    public static final int LOW_SUPPLY = 50;

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a Miner");

        waitForSupplies();

        while (true) {
            if (rc.isCoreReady()) {
                if (rc.getSupplyLevel() > LOW_SUPPLY) {
                    pursueMining();
                } else {
                    goToHQ();
                }
            }

            rc.yield();
        }
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
