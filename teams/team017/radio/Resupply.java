package team017.radio;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Resupply extends RadioModule {
    public static final int NUM_RALLY_POINTS = 10;

    private final int location_slot;
    private final int amount_slot;

    Resupply(RobotController rc, int lowestSlot) {
        super(rc, lowestSlot);
        location_slot = lowestSlot;
        amount_slot = lowestSlot + 1;
    }

    @Override
    public int slotsRequired() {
        return 2;
    }

    // Used for requesting resupply from the robots location.
    // Return whether the request was issued.
    public boolean request(int amount) {
        // Don't overwrite someone else's request.
        if (rx(location_slot) != 0)
            return false;
        tx(location_slot, encodeLocation(rc.getLocation()));
        tx(amount_slot, amount);
        return true;
    }

    // Whether a resupply has been requested.
    public boolean requested() {
        return rx(location_slot) != 0;
    }

    public void clearRequest() {
        tx(location_slot, 0);
    }

    // Get the location of the resupply request.
    public MapLocation getLocation() {
        int encodedLoc = rx(location_slot);
        if (encodedLoc == 0) return null;
        return decodeLocation(encodedLoc);
    }
}
