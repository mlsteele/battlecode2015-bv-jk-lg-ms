package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

// Subclass for units that move and shoot and stuff.
public abstract class Unit extends Robot {
    protected Direction forward;
    protected MapLocation hqLoc;

    protected int rallyGroup;

    Direction cameFrom;
    boolean buggingDirection;

    Unit(RobotController rc) {
        super(rc);
        forward = randomDirection();
        hqLoc = rc.senseHQLocation();
        rallyGroup = Strategy.armyGroupFromRound(Clock.getRoundNum());
        cameFrom = NORTH; // arbitrary initialization
        buggingDirection = rand.nextBoolean();
    }

    // Return to the HQ.
    // Blocking method.
    protected void goToHQ() {
        rc.setIndicatorString(1, "Going back to HQ");
        while (true) {

            if (rc.isCoreReady()) moveTowardBugging(hqLoc);

            if (hqLoc.distanceSquaredTo(rc.getLocation()) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
                rc.setIndicatorString(1, "Back at HQ");
                return;
            }

            rc.yield();
        }
    }

    protected void dumpSuppliesToHQ() {
        rc.setIndicatorString(1, "Dumping supplies...");
        try {
            rc.transferSupplies(Integer.MAX_VALUE, hqLoc);
            rc.setIndicatorString(1, "Dumped supplies.");
            return;
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    // Wander the field aimlessly.
    // Returns whether movement occurred.
    // Assumes CoreReady
    // TOOD(miles): If this gets used, please avoid walking into the line of fire.
    protected boolean wander() {
        // MapLocation target = rc.getLocation().add(forward);
        for (int i = 0; i < 4; i++) {
            if (moveForwardBugging()) {
                return true;
            } else {
                forward = forward.rotateRight().rotateRight();
            }
        }
        return false;
    }

    // Do whatever the default getting-to-rally-point movement is.
    protected boolean moveToRallyPoint(MapLocation loc) {
        return moveToClump(MOVEMENT_CLUMP_DEFAULT, loc);
    }

    protected boolean moveTowardBuggingBlocking(MapLocation loc) {
        while (true) {
            if(rc.isCoreReady()) moveTowardBugging(loc);
            if(rc.getLocation().equals(loc)) return true;
            rc.yield();
        }
    }

    protected boolean moveTowardBugging(MapLocation loc) {
        return moveTowardBugging(rc.getLocation().directionTo(loc));
    }

    protected boolean moveTowardBugging(Direction dir) {
        if (dir == OMNI) return false;
        forward = dir;
        return moveForwardBugging();
    }

    // Tries to move forward while bugging.
    // Returns whether successful.
    // Assumes CoreReady
    protected boolean moveForwardBugging() {
        // try forwards
        if (moveForwardStrict()) return true;

        // try leftish but not if that's where we just came from.
        forward = buggingDirection ? forward.rotateLeft() : forward.rotateRight();
        if (forward != cameFrom) {
            if (moveForwardStrict()) return true;
        }

        // try rightish
        forward = buggingDirection ? forward.rotateRight() : forward.rotateLeft();
        forward = buggingDirection ? forward.rotateRight() : forward.rotateLeft();
        if (moveForwardStrict()) return true;

        // try right
        forward = buggingDirection ? forward.rotateRight() : forward.rotateLeft();
        if (moveForwardStrict()) return true;

        // try right-back
        forward = buggingDirection ? forward.rotateRight() : forward.rotateLeft();
        if (moveForwardStrict()) return true;

        // give up
        return false;
    }

    private boolean moveToCircle(int distanceSquaredTo, MapLocation loc) {
        if (rc.getLocation().distanceSquaredTo(loc) > distanceSquaredTo) {
            return moveTowardBugging(loc);
        } else {
            return false;
        }
    }

    // Move to within `distanceSquared` of `loc`.
    // Tries to get closer even if within `distanceSquared`, but not very hard.
    // Returns whether movement occurred.
    private boolean moveToClump(int distanceSquaredTo, MapLocation loc) {
        if (rc.getLocation().distanceSquaredTo(loc) > distanceSquaredTo) {
            return moveTowardBugging(loc);
        } else {
            return moveCloserTo(loc);
        }
    }

    // Move closer to a location.
    // Guarantees that any step will further the cause.
    private boolean moveCloserTo(MapLocation loc) {
        Direction[] possibleDirs = {NORTH, NORTH_EAST, EAST, SOUTH_EAST,
                                    SOUTH, SOUTH_WEST, WEST, NORTH_WEST};
        int startingDistance = loc.distanceSquaredTo(rc.getLocation());
        for (int i = 0; i < 8; i++) {
            forward = possibleDirs[i];
            if (loc.distanceSquaredTo(rc.getLocation().add(forward)) < startingDistance)
                if (moveForwardStrict())
                    return true;
        }
        return false;
    }

    protected boolean moveTowardStrict(MapLocation loc) {
        forward = rc.getLocation().directionTo(loc);
        return moveForwardStrict();
    }

    protected boolean moveForwardStrict() {
        if (rc.canMove(forward)) {
            try {
                rc.move(forward);
                cameFrom = forward.opposite();
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // dump supplies to neighbor with the lowest supply
    protected boolean dumpSuppliesToNeighbor() {
        RobotInfo[] neighbors = rc.senseNearbyRobots(GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED, rc.getTeam());
        if (neighbors.length == 0) return false;
        RobotInfo neediestNeighbor = neighbors[0];
        double lowestSupply = neighbors[0].supplyLevel;
        for (RobotInfo neighbor : neighbors) {
            if (neighbor.supplyLevel < lowestSupply) {
                lowestSupply = neighbor.supplyLevel;
                neediestNeighbor = neighbor;
                if (lowestSupply == 0) break;
            }
        }
        try {
            rc.transferSupplies((int)rc.getSupplyLevel(), neediestNeighbor.location);
            return true;
        } catch (GameActionException e) {
            e.printStackTrace();
            return false;
        }
    }
}
