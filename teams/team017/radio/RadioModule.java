package team017.radio;

import team017.*;
import battlecode.common.*;

public abstract class RadioModule {
    protected RobotController rc;
    protected int lowestSlot;

    private MapLocation hqLoc;

    RadioModule(RobotController rc, int lowestSlot) {
        this.rc = rc;
        this.lowestSlot = lowestSlot;
        hqLoc = rc.senseHQLocation();
    }

    // How many radio slots the module requires.
    // This is non-static because java sucks.
    // Please do not implement this to depend on instance parameters.
    abstract public int slotsRequired();

    // Encode a location into the lower 16 bits of an int.
    protected int encodeLocation(MapLocation loc) {
        if (loc == null) return 0;
        // The maximum map size is 120x120 [bcd10]
        // We need ~6.9 bits to represent each axis.
        // Map coordinate boundaries are random (or at least ugly)
        // So we use hqLoc as a reference point.
        // This sortof kindof increases the maximum length of
        // an axis to 240, which is ~8 bits.
        // Don't change this to let zero be a valid location
        MapLocation rel = loc.add(-hqLoc.x, -hqLoc.y).add(120, 120);
        return (rel.y << 8) | rel.x;
    }

    protected MapLocation decodeLocation(int loc) {
        if (loc == 0) return null;
        MapLocation rel = new MapLocation(loc & 0xFF, loc >> 8);
        return rel.add(hqLoc.x, hqLoc.y).add(-120, -120);
    }

    protected int encodeTask(Task task) {
        // See Task for bit counts.
        // [16 bits] [12 bits] [4 bits ]
        // [loc    ] [amount ] [taskNum]
        int loc    = (task.loc != null) ? encodeLocation(task.loc) : 0;
        int amount = task.amount / 100;
        return (loc << 16) | (amount << 4) | task.taskNum;
    }

    protected Task decodeTask(int task) {
        return new Task(
            task & 0xF,
            decodeLocation(task >>> 16),
            (task >>> 4 & 0xFFF) * 100);
    }

    // Receive from radio.
    // `channel` must be [0, BROADCAST_MAX_CHANNELS]
    // Wrapper for catching exceptions.
    protected int rx(int channel) {
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
    protected void tx(int channel, int data) {
        try {
            rc.broadcast(channel, data);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
