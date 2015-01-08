package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import static battlecode.common.Direction.*;

import java.util.*;

public class Miner extends Unit {
    Miner(RobotController rc) { super(rc); }

    public static final int STARTING_SUPPLY = 3000;

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a Miner");

        waitForSupplies();

        // Main loop
        // Just hang out for now
        while(true) {
            Direction forward = NORTH;

            if (rc.isCoreReady()) {
                if (rc.senseOre(rc.getLocation()) > 0) {
                    mine();
                } else {
                    forward = optimalOreDirection();
                    moveForward();
                }
            }

            rc.setIndicatorString(0, "Best direction is " + optimalOreDirection());
            rc.yield();
        }
    }

    public Direction optimalOreDirection() {
        /* Directions: NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORHT_WEST, NONE*/
        Direction[] possibleDirs = {NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH,
                                    SOUTH_WEST, WEST, NORTH_WEST};

        MapLocation curLocation = rc.getLocation();
        double bestOre = 0;
        Direction bestDirection = NORTH;

        for (Direction d : possibleDirs) {
            double ore = rc.senseOre(curLocation.add(d));
            if (ore > bestOre && rc.canMove(d)){
                bestOre = ore;
                bestDirection = d;
            }
        }

        return bestDirection;
    }

    // returns true if can mine
    // assumes there is ore
    private boolean mine() {
        if(rc.canMine()) {
            try {
                rc.mine();
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

}
