package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

// Subclass for units that move and shoot and stuff.
public abstract class Unit extends Robot {
    protected Direction forward;
    protected MapLocation hqLoc;

    Unit(RobotController rc) {
        super(rc);
        forward = randomDirection();
        hqLoc = rc.senseHQLocation();
    }

    // Tries to move dir.
    // Returns whether successful.
    // Assumes CoreReady
    protected boolean moveForward() {
        if (rc.canMove(forward)) {
            try {
                rc.move(forward);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            try {
                if ((rand.nextInt(2)) == 0) {
                    forward = forward.rotateRight();
                } else {
                    forward = forward.rotateLeft();
                }
                if (rc.canMove(forward)) {
                    rc.move(forward);
                }
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    protected boolean moveToward(MapLocation loc) {
        return moveToward(rc.getLocation().directionTo(loc));
    }

    protected boolean moveToward(Direction dir) {
        forward = dir;
        return moveForward();
    }

    // Return to the HQ.
    // Blocking method.
    protected void goToHQ() {
        rc.setIndicatorString(1, "Going back to HQ");
        while (true) {

            if (rc.isCoreReady()) moveToward(hqLoc);

            if (hqLoc.distanceSquaredTo(rc.getLocation()) <= GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED) {
                rc.setIndicatorString(1, "Back at HQ");
                return;
            }

            rc.yield();
        }
    }


    // Wander the field aimlessly.
    // Returns whether movement occurred.
    // Assumes CoreReady
    // TOOD(miles): If this gets used, please avoid walking into the line of fire.
    protected boolean wander() {
        // MapLocation target = rc.getLocation().add(forward);
        for (int i = 0; i < 4; i++) {
            if (moveForward()) {
                return true;
            } else {
                forward = forward.rotateRight().rotateRight();
            }
        }
        return false;
    }

}
