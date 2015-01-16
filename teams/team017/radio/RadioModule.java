package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.lang.RuntimeException;

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

    protected int encodeLocation(MapLocation loc) {
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
        MapLocation rel = new MapLocation(loc & 0xFF, loc >> 8);
        return rel.add(hqLoc.x, hqLoc.y).add(-120, -120);
    }

    protected int encodeTask(Task task) {
        if (task.loc == null) return task.taskNum;
        return (encodeLocation(task.loc) << 16) | task.taskNum;
    }

    protected Task decodeTask(int task) {
        int encodedLoc = task >> 16;
        if (encodedLoc == 0) return new Task(task);
        int taskNum = task & 0xFFFF;
        return new Task(taskNum, decodeLocation(encodedLoc));
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
