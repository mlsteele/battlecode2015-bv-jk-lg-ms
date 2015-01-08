package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;

import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

// Radio module to abstract away our radio protocol.
// Caches chunks of data.
// 'load' methods read from the broadcast stream (expensive).
// 'get' methods get cached data (will not cause a load, so be careful not to read old/unitialized data).
// 'write' methods change information and broadcast it.
public class RadioFrob {
    private static int RALLY_POINT_1_SLOT = 0;
    private static int NUM_MINING_FACTORIES = 1;
    private static int BEAVER_JOB_ASSIGNMENT_SLOT = 1000;
    private static int BEAVER_JOB_BASE = BEAVER_JOB_ASSIGNMENT_SLOT + 1;

    private RobotController rc;
    private MapLocation hqLoc; // Used for anchoring relative coordinates.
    private MapLocation rallyPoint1;

    private int freeBeaverJobSlot = 0;
    public int myJobSlot = 0;

    RadioFrob(RobotController rc) {
        this.rc = rc;
        hqLoc = rc.senseHQLocation();
    }

    // Assigns a job to the beaver slot. Returns job assignment slot
    // returns -1 if slot has not been claimed
    public int assignBeaverJobSlot() throws GameActionException {
        if (rc.readBroadcast(BEAVER_JOB_ASSIGNMENT_SLOT) != 0) {
            return -1;
        } else {
            freeBeaverJobSlot++;
            rc.broadcast(BEAVER_JOB_ASSIGNMENT_SLOT, freeBeaverJobSlot);
            return freeBeaverJobSlot;
        }
    }

    public int getBeaverJobSlot() throws GameActionException {
        return rc.readBroadcast(BEAVER_JOB_ASSIGNMENT_SLOT);
    }

    public void clearBeaverJobSlot() throws GameActionException {
        rc.broadcast(BEAVER_JOB_ASSIGNMENT_SLOT, 0);
    }

    public int getJob(int jobSlot) throws GameActionException {
        return rc.readBroadcast(BEAVER_JOB_BASE + jobSlot);
    }

    // returns my job
    public int getJob() throws GameActionException {
        System.out.println("Attempting to get job with: " + myJobSlot);
        return getJob(myJobSlot);
    }

    // sets a job for the given beaver job slot
    public boolean setJob(int job, int jobSlot) throws GameActionException {
        System.out.println("Setting job for " + jobSlot + " with " + job);
        rc.broadcast(BEAVER_JOB_BASE + jobSlot, job);
        return true;
    }

    // sets my job
    public boolean setJob(int job) throws GameActionException {
        return setJob(job, myJobSlot);
    }

    //TODO: Use caching later Dont think this is used lol
    public void writeIncMiningFacCount() {
        try {
            int mining_count = rc.readBroadcast(NUM_MINING_FACTORIES);
            rc.broadcast(NUM_MINING_FACTORIES, mining_count++);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    // Load rally point 1
    public void loadRally1() {
        try {
            rallyPoint1 = decodeLocation(rc.readBroadcast(RALLY_POINT_1_SLOT));
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    public MapLocation getRally1() {
        return rallyPoint1;
    }

    public void writeRally1(MapLocation loc) {
        try {
            rallyPoint1 = loc;
            rc.broadcast(RALLY_POINT_1_SLOT, encodeLocation(rallyPoint1));
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }

    private int encodeLocation(MapLocation loc) {
        // The maximum map size is 120x120 [bcd10]
        // We need ~6.9 bits to represent each axis.
        // Map coordinate boundaries are random (or at least ugly)
        // So we use hqLoc as a reference point.
        // This sortof kindof increases the maximum length of
        // an axis to 240, which is ~8 bits.
        MapLocation rel = loc.add(-hqLoc.x, -hqLoc.y).add(120, 120);
        rc.setIndicatorString(2, "encode rel " + rel);
        return (rel.y << 8) | rel.x;
    }

    private MapLocation decodeLocation(int loc) {
        MapLocation rel = new MapLocation(loc & 0xFF, loc >> 8);
        rc.setIndicatorString(2, "decode rel " + rel);
        return rel.add(hqLoc.x, hqLoc.y).add(-120, -120);
    }
}
