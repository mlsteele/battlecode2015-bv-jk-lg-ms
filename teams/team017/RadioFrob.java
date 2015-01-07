package team017;

import battlecode.common.*;
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

    private RobotController rc;
    private MapLocation hqLoc; // Used for anchoring relative coordinates.
    private MapLocation rallyPoint1;

    RadioFrob(RobotController rc) {
        this.rc = rc;
        hqLoc = rc.senseHQLocation();
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
