package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class Drone extends Unit {
    private boolean harassApproachDirection;

    Drone(RobotController rc) { super(rc); }

    @Override
    public void run() {
        harassApproachDirection = rand.nextBoolean();

        waitForSupplies();

        // Main loop
        while (true) {
            rc.setIndicatorString(2, null);

            shootBaddies();

            if (rc.isCoreReady()) wander();

            rc.yield();
        }
    }

    // Wander the field aimlessly.
    // Doesn't wander into enemy fire.
    // Returns whether movement occurred.
    // Assumes CoreReady
    protected boolean wander() {
        // Get out of dodge if we're not safe.
        MapLocation threatener = isLocationSafe(rc.getLocation());
        if (threatener != null)
            return getOutOfDodge(threatener);

        // Pick a direction.
        // Go towards the HQ, or up the sidelines, or a random direction.
        int r = rand.nextInt(10);
        forward = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        if (r < 7)
            forward = harassApproachDirection ? forward.rotateRight() : forward.rotateLeft();
        if (r < 3)
            forward = randomDirection();

        // Make sure it's safe.
        if (isLocationSafe(rc.getLocation().add(forward)) != null) {
            rc.setIndicatorString(2, "decided not to move into that unsafe area");
            return false;
        }

        return moveForwardStrict();
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

    // Drones work differently from most units, so this is overridden.
    // Drones can move 'while' shooting.
    protected void shootBaddies() {
        // Abort if no weapon
        if (!rc.isWeaponReady()) return;

        int range = rc.getType().attackRadiusSquared;
        Team enemy = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(range, enemy);

        // Abort if no enemies in sight.
        if (enemies.length == 0) return;

        try {
            rc.attackLocation(chooseTarget(enemies).location);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    // Chooses which robot to attack.
    private RobotInfo chooseTarget(RobotInfo[] enemies) {
        RobotInfo target = enemies[0];
        double    targetScore = -1;
        for (RobotInfo r : enemies) {
            double score = 0;
            // Fight back
            if (rc.getLocation().distanceSquaredTo(r.location) <= r.type.attackRadiusSquared)
                score = 200;
            // Shoot miners
            if (r.type == MINER)
                score = 100;
            // Buildings
            if (r.type == TOWER)               score = 11;
            if (r.type == HQ)                  score = 10;
            if (r.type == SUPPLYDEPOT)         score = 9;
            if (r.type == AEROSPACELAB)        score = 8;
            if (r.type == TANKFACTORY)         score = 7;
            if (r.type == HELIPAD)             score = 6;
            if (r.type == MINERFACTORY)        score = 5;
            if (r.type == HANDWASHSTATION)     score = 4;
            if (r.type == BARRACKS)            score = 3;
            if (r.type == TRAININGFIELD)       score = 2;
            if (r.type == TECHNOLOGYINSTITUTE) score = 1;

            if (score > targetScore) {
                target = r;
                targetScore = score;
            }
        }
        return target;
    }
}
