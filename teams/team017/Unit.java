package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

// Subclass for units that move and shoot and stuff.
public abstract class Unit extends Robot {
    protected Direction forward;

    Unit(RobotController rc) {
        super(rc);
        forward = randomDirection();
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
                if ((rand.nextDouble() * 2) <= 1) {
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
        forward = rc.getLocation().directionTo(loc);
        return moveForward();
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
