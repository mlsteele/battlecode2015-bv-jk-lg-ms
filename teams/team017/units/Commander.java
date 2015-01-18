package team017.units;

import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import team017.*;
import battlecode.common.*;

public class Commander extends Unit {

    public Commander(RobotController rc) { super(rc); }
    private boolean harassApproachDirection;
    private int COMMANDER_LOW_HEALTH = 50;

    @Override
    public void run() {
        harassApproachDirection = rand.nextBoolean();

        // Main loop
        while (true) {

            rc.setIndicatorString(0, String.valueOf(rc.getHealth()));

            if (rc.isCoreReady()) {
                // Check our health, if its below the low health threshold, lets get
                // retreat and heal
                if (rc.getHealth() <= COMMANDER_LOW_HEALTH) {
                    rc.setIndicatorString(1, "RETREAT");
                    if (enemiesNearby()) fastRetreatToHeal();
                    else rc.yield(); // lets just chill and heal
                    continue;
                }

                shootBaddies(COMMANDER_LOW_HEALTH);

                wander();
            }

            rc.yield();
        }
    }

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

    protected boolean enemiesNearby() {
        RobotInfo[] enemies = rc.senseNearbyRobots(
                rc.getType().sensorRadiusSquared,
                rc.getTeam().opponent()
        );

        if(enemies.length > 0) return true;
        return false;
    }

    // Assumes core ready, heads in the direction of our HQ
    // should only be used by commander
    // TODO(Brandon): please make this smarter
    protected void fastRetreatToHeal() {

        rc.setIndicatorString(1, "Using fast retreat");
        // Lets try and flash first
        MapLocation myLoc = rc.getLocation();

        if (safeFlashCooldown() == 0) {
            Direction toHq = myLoc.directionTo(hqLoc);

            for (int distance = 2; distance > 1; distance--) {
                MapLocation target = myLoc.add(toHq, distance);
                System.out.println("this is my location: " + myLoc.toString());
                System.out.println("This is my target: " + target.toString());
                System.out.println("This is the distance: " + myLoc.distanceSquaredTo(target));

                // attempt to jump the furthest
                if (rc.isPathable(COMMANDER, target)) {
                    safeFlash(target);
                    return;
                }
            }

        }

        // we cant flash so lets at least try and run in the direction towards hq


    }

    protected int safeFlashCooldown() {
        try { return rc.getFlashCooldown(); }
        catch (GameActionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Assumes you can flash somwhere
    protected void safeFlash(MapLocation target) {
        try { rc.castFlash(target); }
        catch (GameActionException e) { e.printStackTrace(); }
    }
}
