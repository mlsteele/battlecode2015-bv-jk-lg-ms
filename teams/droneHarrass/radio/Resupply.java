package droneHarrass.radio;

import battlecode.common.*;

// Resupply requests for structures.
// One request can be outstanding at a time.
// Methods are marked with which unit should use them.
public class Resupply extends RadioModule {
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

    // Whether a resupply has been requested.
    // HQ ONLY
    public boolean isRequested() {
        return rx(location_slot) != 0;
    }

    // Acknowledge a supply request.
    // Unblocks the channel so others can request.
    // HQ ONLY
    public void clearRequest() {
        tx(location_slot, 0);
    }

    // Get the location of the resupply request.
    // Valid only if isRequested is true.
    // HQ ONLY
    public MapLocation getLocation() {
        int encodedLoc = rx(location_slot);
        if (encodedLoc == 0) return null;
        return decodeLocation(encodedLoc);
    }

    // Get the amount requested.
    // Valid only if isRequested is true.
    // HQ ONLY
    public int getAmount() {
        return rx(amount_slot);
    }

    // Request a supply delivery at our location.
    // Returns whether the request was issued.
    // NON-HQ ONLY
    public boolean request(int amount) {
        // Don't overwrite someone else's request.
        if (rx(location_slot) != 0)
            return false;
        tx(location_slot, encodeLocation(rc.getLocation()));
        tx(amount_slot, amount);
        return true;
    }
}
