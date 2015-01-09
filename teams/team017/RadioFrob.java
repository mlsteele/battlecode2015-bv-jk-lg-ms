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

    private static int BEAVER_TASK_ASSIGNMENT_SLOT = 1000;
    private static int BEAVER_TASK_BASE = BEAVER_TASK_ASSIGNMENT_SLOT + 1;

    private RobotController rc;
    private MapLocation hqLoc; // Used for anchoring relative coordinates.
    private MapLocation[] rallyPoints = new MapLocation[RALLY_POINT_RANGE_SIZE];

    private int freeBeaverTaskSlot = 0;
    public int myTaskSlot = 0;

    RadioFrob(RobotController rc) {
        this.rc = rc;
        hqLoc = rc.senseHQLocation();
    }

    // Assigns a task to the beaver slot. Returns task assignment slot
    // returns -1 if slot has not been claimed
    public int assignBeaverTaskSlot() {
        if (rx(BEAVER_TASK_ASSIGNMENT_SLOT) != 0) {
            return -1;
        } else {
            freeBeaverTaskSlot++;
            tx(BEAVER_TASK_ASSIGNMENT_SLOT, freeBeaverTaskSlot);
            return freeBeaverTaskSlot;
        }
    }

    // gets the taskSlot of the beaver who was assigned one (used by beaver)
    // clears it to let the hq know it got its taskSlot
    public int getBeaverTaskSlot() {
        int taskSlot = rx(BEAVER_TASK_ASSIGNMENT_SLOT);
        tx(BEAVER_TASK_ASSIGNMENT_SLOT, 0);
        return taskSlot;
    }

    // returns the task at a given taskSlot
    public Task getTask(int taskSlot) {
        return decodeTask(rx(BEAVER_TASK_BASE + taskSlot));
    }

    // sets a task for the given beaver taskSlot
    public boolean setTask(Task task, int taskSlot) {
        tx(BEAVER_TASK_BASE + taskSlot, encodeTask(task));
        return true;
    }

    // used by beaver to request a task
    public boolean requestTask() {
        tx(BEAVER_TASK_BASE, myTaskSlot);
        tx(myTaskSlot, -1);
        return true;
    }

    // used by hq, returns the taskslot of the beaver assigned the task
    public int assignTaskToNextFree(Task task) {
        int taskSlot = rx(BEAVER_TASK_BASE);
        System.out.println("This is what was the taskslot" + taskSlot);
        if (taskSlot > 0 && (rx(taskSlot) == -1)) {
            System.out.println("Giving taskSlot " + taskSlot + " task " + task);
            tx(BEAVER_TASK_BASE, 0);
            setTask(task, taskSlot);
            return taskSlot;
        } else return -1;
    }

    public boolean requestResupply(int amount) {
        // someone else is requesting right now
        if (rx(REQUEST_RESUPPLY_LOCATION_SLOT) != 0) return false;
        tx(REQUEST_RESUPPLY_LOCATION_SLOT, encodeLocation(rc.getLocation()));
        tx(REQUEST_RESUPPLY_AMOUNT_SLOT, amount);
        return true;
    }

    public boolean resupplyFromTankFactoryRequested() {
            return rx(REQUEST_RESUPPLY_LOCATION_SLOT) != 0;
    }

    public boolean clearResupplyRequest() {
        tx(REQUEST_RESUPPLY_LOCATION_SLOT, 0);
        return true;
    }

    public MapLocation getResupplyLocation() {
        int encodedLoc = rx(REQUEST_RESUPPLY_LOCATION_SLOT);
        if (encodedLoc == 0) return null;
        return decodeLocation(encodedLoc);
    }

    //TODO: Use caching later Dont think this is used lol
    public void writeIncMiningFacCount() {
        int mining_count = rx(NUM_MINING_FACTORIES);
        tx(NUM_MINING_FACTORIES, mining_count++);
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

    private int encodeTask(Task task) {
        if (task.loc == null) return task.taskNum;
        return (encodeLocation(task.loc) << 16) | task.taskNum;
    }

    private Task decodeTask(int task) {
        int encodedLoc = task >> 16;
        if (encodedLoc == 0) return new Task(task);
        int taskNum = task & 0xFFFF;
        return new Task(taskNum, decodeLocation(encodedLoc));
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
