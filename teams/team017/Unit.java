package team017;

import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import battlecode.common.*;

// Subclass for units that move and shoot and stuff.
public abstract class Unit extends Robot {
    protected Direction forward;
    protected MapLocation hqLoc;

    protected int rallyGroup;

    Direction cameFrom;
    boolean buggingDirection;

    public Unit(RobotController rc) {
            super(rc);
            forward = randomDirection();
            hqLoc = rc.senseHQLocation();
            rallyGroup = Strategy.armyGroupFromRound(Clock.getRoundNum());
            cameFrom = NORTH; // arbitrary initialization
            buggingDirection = rand.nextBoolean();
    }

    // Attacking for units.
    // Blocks until no enemies are seen.
    protected void shootBaddies(int health) {
        int range = rc.getType().attackRadiusSquared;
        Team enemy = rc.getTeam().opponent();

        // Keep scanning until either a hit or no enemies in sight.
        // It's not good to shuffle around, causing further loading delay
        // when an enemy is in range.
        while (true) {
            if (rc.getHealth() <= health) return;
            RobotInfo[] enemies = rc.senseNearbyRobots(range, enemy);

            // Return if all clear.
            if (enemies.length == 0)
                return;

            if (rc.isWeaponReady()) {
                try {
                    // Note: canAttackLocation seems useless (see engine source)
                    rc.attackLocation(chooseTarget(enemies).location);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }

            rc.yield();
        }
    }

    protected void shootBaddies() { shootBaddies(0); }

    // Move towards a mobile enemy.
    protected void pursueMobileEnemy() {
        RobotInfo[] enemies = rc.senseNearbyRobots(
            rc.getType().sensorRadiusSquared,
            rc.getTeam().opponent());

        if (enemies.length > 0) {
            moveTowardBugging(chooseMobileTarget(enemies).location);
        }
    }

    // Return to the HQ.
    // Blocking method.
    // Does not move in already in range of HQ.
    protected void goToHQ() {
        rc.setIndicatorString(1, "Going back to HQ");
        while (true) {

            if (hqLoc.distanceSquaredTo(rc.getLocation()) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
                rc.setIndicatorString(1, "Back at HQ");
                return;
            }

            if (rc.isCoreReady()) moveTowardBugging(hqLoc);

            rc.yield();
        }
    }

    // Idle until any supplies are received.
    // Also shoot.
    // Return the amount of supplies received.
    protected int waitForSupplies() {
        double supplyLevel;

        boolean shouldAttack = (rc.getType() != BASHER) && (rc.getType() != BEAVER);
        while ((supplyLevel = rc.getSupplyLevel()) < 1) {
            if (shouldAttack) shootBaddies();
            rc.yield();
        }
        return (int)supplyLevel;
    }

    // Requires that we are within supply range of HQ.
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
        return moveTowardBugging(loc, false);
    }

    protected boolean moveTowardBugging(MapLocation loc, boolean beSafe) {
        return moveTowardBugging(rc.getLocation().directionTo(loc), beSafe);
    }

    protected boolean moveTowardBugging(Direction dir) {
        return moveTowardBugging(dir, false);
    }

    protected boolean moveTowardBugging(Direction dir, boolean beSafe) {
        if (dir == OMNI) return false;
        forward = dir;
        return moveForwardBugging(beSafe);
    }

    protected boolean moveForwardBugging() {
        return moveForwardBugging(false);
    }

    // Tries to move forward while bugging.
    // Returns whether successful.
    // Assumes CoreReady
    protected boolean moveForwardBugging(boolean beSafe) {
        // try forwards
        if (moveForwardStrict(beSafe)) return true;

        // try leftish but not if that's where we just came from.
        forward = buggingDirection ? forward.rotateLeft() : forward.rotateRight();
        if (forward != cameFrom) {
            if (moveForwardStrict(beSafe)) return true;
        }

        // try rightish
        forward = buggingDirection ? forward.rotateRight() : forward.rotateLeft();
        forward = buggingDirection ? forward.rotateRight() : forward.rotateLeft();
        if (moveForwardStrict(beSafe)) return true;

        // try right
        forward = buggingDirection ? forward.rotateRight() : forward.rotateLeft();
        if (moveForwardStrict(beSafe)) return true;

        // try right-back
        forward = buggingDirection ? forward.rotateRight() : forward.rotateLeft();
        if (moveForwardStrict(beSafe)) return true;

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
        return moveForwardStrict(false);
    }

    protected boolean moveForwardStrict() {
        return moveForwardStrict(false);
    }

    protected boolean moveForwardStrict(boolean beSafe) {
        if (rc.canMove(forward) && (!beSafe || isLocationSafe(rc.getLocation().add(forward)) == null)) {
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

    // Returns NULL if the location is safe, or the location of one of the enemies threatening the location.
    // NOTE: Considers being under fire by BEAVERS and MINERS safe.
    protected MapLocation isLocationSafe(MapLocation loc) {
        // TODO(miles): consider enemy weapons cooldown.
        int range = MAXIMUM_ATTACK_RANGE_EVER;
        Team enemy = rc.getTeam().opponent();
        RobotInfo[] enemies      = rc.senseNearbyRobots(loc, range, enemy);
        MapLocation[] towers     = rc.senseEnemyTowerLocations();
        MapLocation   evilHqLoc  = rc.senseEnemyHQLocation();

        // Consider HQ
        // TODO(miles): consider HQ boost radius.
        if (loc.distanceSquaredTo(evilHqLoc) <= HQ.attackRadiusSquared)
            return evilHqLoc;

        // Consider Towers
        for (MapLocation towerLoc : towers) {
            if (loc.distanceSquaredTo(towerLoc) <= TOWER.attackRadiusSquared)
                return towerLoc;
        }

        // Consider Enemies
        if (enemies.length == 0) return null;
        for (RobotInfo r : enemies) {
            // Special case: ignore beavers and miners because they're weaklings.
            if (r.type == BEAVER || r.type == MINER) continue;

            if (loc.distanceSquaredTo(r.location) <= r.type.attackRadiusSquared)
                return r.location;
        }

        return null;
    }

    // Get out of an unsafe location.
    // Go to any adjacent safe location.
    // `threatener` is the location of a spooky skeleton.
    // Try to get to complete safety, but failing that, run away from the spooky skeleton.
    protected boolean getOutOfDodge(MapLocation threatener) {
        rc.setIndicatorString(2, "getting out of dodge");
        Direction[] possibleDirs = {NORTH, NORTH_EAST, EAST, SOUTH_EAST,
                SOUTH, SOUTH_WEST, WEST, NORTH_WEST};
        MapLocation target;

        for (int i = 0; i < 8; i++) {
            target = rc.getLocation().add(possibleDirs[i]);
            if (rc.canMove(possibleDirs[i]) && isLocationSafe(target) == null) {
                forward = possibleDirs[i];
                try {
                    rc.move(forward);
                    return true;
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            }
        }

        // We didn't manage to find sanctuary adjacent.
        // Just try to run from the spooky skeleton.
        forward = rc.getLocation().directionTo(threatener).opposite();
        moveForwardBugging();
        return false;
    }
}
