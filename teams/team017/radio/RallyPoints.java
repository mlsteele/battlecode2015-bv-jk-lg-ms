package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.lang.RuntimeException;

public class RallyPoints extends RadioModule {
    public static final int NUM_RALLY_POINTS = 10;

    RallyPoints(RobotController rc, int lowestSlot) {
        super(rc, lowestSlot);
    }

    @Override
    public int slotsRequired() {
        return NUM_RALLY_POINTS;
    }

    // Get the nth rally point.
    // n must be in [0, NUM_RALLY_POINTS)
    public MapLocation get(int n) {
        if (n < 0 || n > NUM_RALLY_POINTS)
            throw new RuntimeException("rally point "+n+" out of bounds");
        return decodeLocation(rx(lowestSlot + n));
    }

    // Get the nth rally point.
    // n must be in [0, NUM_RALLY_POINTS)
    public void set(int n, MapLocation loc) {
        if (n < 0 || n > NUM_RALLY_POINTS)
            throw new RuntimeException("rally point "+n+" out of bounds");
        tx(lowestSlot + n, encodeLocation(loc));
    }
}
