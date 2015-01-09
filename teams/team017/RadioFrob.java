package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

import javax.sound.midi.SysexMessage;

import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

// Radio module to abstract away our radio protocol.
// Caches chunks of data.
// 'load' methods read from the broadcast stream (expensive).
// 'get' methods get cached data (will not cause a load, so be careful not to read old/unitialized data).
// 'write' methods change information and broadcast it.
public class RadioFrob {
    // 0    | unused
    // ------
    // 1    | undocumented
    //      |
    // ------
    // 3000 | rally points
    // 3009 |
    // ------
    //      | unused
    private static final int RALLY_POINT_RANGE_BOTTOM = 3000;
    private static final int RALLY_POINT_RANGE_SIZE   = 10;

    private static int NUM_MINING_FACTORIES = 1;

    private static int REQUEST_RESUPPLY_LOCATION_SLOT = 1;
    private static int REQUEST_RESUPPLY_AMOUNT_SLOT = 2;

    private static int BEAVER_JOB_ASSIGNMENT_SLOT = 1000;
    private static int BEAVER_JOB_BASE = BEAVER_JOB_ASSIGNMENT_SLOT + 1;

    private RobotController rc;
    private MapLocation hqLoc; // Used for anchoring relative coordinates.
    private MapLocation[] rallyPoints = new MapLocation[RALLY_POINT_RANGE_SIZE];

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
        int jobSlot = rc.readBroadcast(BEAVER_JOB_ASSIGNMENT_SLOT);
        rc.broadcast(BEAVER_JOB_ASSIGNMENT_SLOT, 0);
        return jobSlot;
    }

    public Job getJob(int jobSlot) throws GameActionException {
        return decodeJob(rc.readBroadcast(BEAVER_JOB_BASE + jobSlot));
    }

    // sets a job for the given beaver job slot
    public boolean setJob(Job job, int jobSlot) throws GameActionException {
        rc.broadcast(BEAVER_JOB_BASE + jobSlot, encodeJob(job));
        return true;
    }

    // used by beaver
    public boolean requestJob() throws GameActionException {
        rc.broadcast(BEAVER_JOB_BASE, myJobSlot);
        rc.broadcast(myJobSlot, -1);
        return true;
    }

    // used by hq, returns the jobslot of the beaver assigned the job
    public int assignJobToNextFree(Job job) throws GameActionException {
        int jobSlot = rc.readBroadcast(BEAVER_JOB_BASE);
        System.out.println("This is what was the jobslot" + jobSlot);
        if (jobSlot > 0 && (rc.readBroadcast(jobSlot) == -1)) {
            System.out.println("Giving jobSlot " + jobSlot + " job " + job);
            // cool we actually got a beaver whos waiting
            // lets clear the job slot and give them the job
            rc.broadcast(BEAVER_JOB_BASE, 0);
            setJob(job, jobSlot);
            return jobSlot;
        } else return -1;
    }

    public boolean requestResupply(int amount) {
        try {
            // someone else is requesting right now
            if (rc.readBroadcast(REQUEST_RESUPPLY_LOCATION_SLOT) != 0) return false;
            rc.broadcast(REQUEST_RESUPPLY_LOCATION_SLOT, encodeLocation(rc.getLocation()));
            rc.broadcast(REQUEST_RESUPPLY_AMOUNT_SLOT, amount);
            return true;
        } catch (GameActionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resupplyFromTankFactoryRequested() {
        try {
            return rc.readBroadcast(REQUEST_RESUPPLY_LOCATION_SLOT) != 0;
        } catch (GameActionException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean clearResupplyRequest() {
        try {
            rc.broadcast(REQUEST_RESUPPLY_LOCATION_SLOT, 0);
            return true;
        } catch (GameActionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public MapLocation getResupplyLocation() {
        try {
            int encodedLoc = rc.readBroadcast(REQUEST_RESUPPLY_LOCATION_SLOT);
            if (encodedLoc == 0) return null;
            return decodeLocation(encodedLoc);
        } catch (GameActionException e) {
            return null;
        }
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

    // Load rally point n
    public void loadRally(int n) {
        if (n < 0 || n > RALLY_POINT_RANGE_SIZE)
            throw new RuntimeException("rally point "+n+" out of bounds");
        rallyPoints[n] = decodeLocation(rx(RALLY_POINT_RANGE_BOTTOM + n));
    }

    public MapLocation getRally(int n) {
        if (n < 0 || n > RALLY_POINT_RANGE_SIZE)
            throw new RuntimeException("rally point "+n+" out of bounds");
        return rallyPoints[n];
    }

    public void writeRally(int n, MapLocation loc) {
        if (n < 0 || n > RALLY_POINT_RANGE_SIZE)
            throw new RuntimeException("rally point "+n+" out of bounds");
        rallyPoints[n] = loc;
        tx(RALLY_POINT_RANGE_BOTTOM + n, encodeLocation(loc));
    }

    private int encodeLocation(MapLocation loc) {
        // The maximum map size is 120x120 [bcd10]
        // We need ~6.9 bits to represent each axis.
        // Map coordinate boundaries are random (or at least ugly)
        // So we use hqLoc as a reference point.
        // This sortof kindof increases the maximum length of
        // an axis to 240, which is ~8 bits.
        MapLocation rel = loc.add(-hqLoc.x, -hqLoc.y).add(120, 120);
        return (rel.y << 8) | rel.x;
    }

    private MapLocation decodeLocation(int loc) {
        MapLocation rel = new MapLocation(loc & 0xFF, loc >> 8);
        return rel.add(hqLoc.x, hqLoc.y).add(-120, -120);
    }

    private int encodeJob(Job job) {
        if (job.loc == null) return job.jobNum;
        return (encodeLocation(job.loc) << 16) | job.jobNum;
    }

    private Job decodeJob(int job) {
        int encodedLoc = job >> 16;
        if (encodedLoc == 0) return new Job(job);
        int jobNum = job & 0xFFFF;
        return new Job(jobNum, decodeLocation(encodedLoc));
    }

    // Receive from radio.
    // `channel` must be [0, BROADCAST_MAX_CHANNELS]
    // Wrapper for catching exceptions.
    private int rx(int channel) {
        try {
            return rc.readBroadcast(channel);
        } catch (GameActionException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Transmit through radio.
    // `channel` must be [0, BROADCAST_MAX_CHANNELS]
    // Wrapper for catching exceptions.
    private void tx(int channel, int data) {
        try {
            rc.broadcast(channel, data);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
