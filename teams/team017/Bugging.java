package team017;

import java.util.*;
import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import team017.*;
import team017.radio.*;
import team017.structures.*;
import team017.units.*;
import static team017.Strategy.*;

// Bugging movement controller.
// Stateful.
public enum Bugging {;
    private static int thresholdDistanceSq = MOVEMENT_CLUMP_DEFAULT;
    private static MapLocation target;
    private static boolean staySafe;
    private static Direction forward;
    // Where bugging started
    // NULL if not currently bugging.
    private static MapLocation start;

    // Setup controller parameters
    // Does NOT change bugging state.
    // Therefore, can be called while bugging to change parameters.
    public static void setParams(MapLocation target, int thresholdDistanceSq, boolean staySafe) {
        Bugging.target = target;
        Bugging.thresholdDistanceSq = thresholdDistanceSq;
        Bugging.staySafe = staySafe;
    }

    // Try to move forward while bugging.
    // Returns whether a move occurred.
    // Assumes CoreReady
    public static boolean move() {
        S.rc.setIndicatorString(2, "move");
        if (target == null) {
            System.err.println("ERROR: tried to move without target");
            return false;
        }

        // Check if we are close enough.
        int distanceSqToTarget = S.rc.getLocation().distanceSquaredTo(target);
        if (distanceSqToTarget <= thresholdDistanceSq) {
            // Stop bugging.
            start = null;
            S.rc.setIndicatorString(2, "close enough");
            return moveCloserToTarget();
        }

        if (start == null) {
            // Not currently bugging.
            forward = S.rc.getLocation().directionTo(target);
            S.rc.setIndicatorString(2, "not buggin");
            if (moveForwardish()) return true;
            // Start bugging.
            start = S.rc.getLocation();
            forward = forward.rotateRight().rotateRight();
            return move();
        } else {
            // Already bugging.
            // Stop bugging if we got closer to the target than when we started bugging.
            if (distanceSqToTarget < start.distanceSquaredTo(target)) {
                start = null;
                return move();
            }

            // Stop bugging if back-left is clear.
            // This means that we must have bugged around something that has since moved.
            if (canMove(forward.rotateLeft().rotateLeft().rotateLeft())) {
                start = null;
                forward = S.rc.getLocation().directionTo(target);
                S.rc.setIndicatorString(2, "back left clear, forward " + forward);
                return moveForwardish();
            }

            S.rc.setIndicatorString(2, "scan circle");
            forward = forward.rotateLeft().rotateLeft();
            // Try moving left, and try every direction in a circle from there.
            for (int i = 0; i < 8; i++) {
                if (moveForwardStrict()) return true;
                forward = forward.rotateRight();
            }
            return false;
        }
    }

    // Try moving forward and it's direct neighbor directions.
    // Can change `forward`.
    private static boolean moveForwardish() {
        if (moveForwardStrict()) return true;
        Direction f = forward;
        forward = f.rotateLeft();
        if (moveForwardStrict()) return true;
        forward = f.rotateRight();
        if (moveForwardStrict()) return true;
        forward = f;
        return false;
    }

    protected static boolean moveForwardStrict() {
        if (canMove(forward)) {
            try {
                S.rc.move(forward);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean canMove(Direction dir) {
        if (!S.rc.canMove(dir)) return false;
        if (staySafe) {
            return null == Unit.isLocationSafe(S.rc.getLocation().add(dir));
        } else {
            return true;
        }
    }

    // Move closer to a location.
    // Guarantees that any step will further the cause.
    // Returns whether movement occurred.
    private static boolean moveCloserToTarget() {
        Direction f = forward;
        int startingDistance = target.distanceSquaredTo(S.rc.getLocation());
        forward = S.rc.getLocation().directionTo(target);
        forward = forward.rotateLeft();
        for (int i = 0; i < 8; i++) {
            if (target.distanceSquaredTo(S.rc.getLocation().add(forward)) < startingDistance)
                if (moveForwardStrict())
                    return true;
            forward = forward.rotateRight();
        }
        forward = f;
        return false;
    }

}
