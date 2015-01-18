package team017.radio;

import static team017.Strategy.*;
import battlecode.common.*;

// THIS MODULES IS NOT USED.
// It costs at least 500 BTC/HQ/Turn because clearing the counters
// is expensive. It's flaky because extra yields in units and the HQ
// can cause miscounts, and we already have getUnitCounts so why did I
// even write this?
//
// Keeps track of the number of live units of every type.
// Usage:
//   HQ starts round by calling reset()
//   Each unit increment()s the counter for their type.
//   HQ saveToCache() the results from the all the counters.
//   HQ readCached()s the counters to get unit counts.
//   Rinse and repeat.
public class UnitCounts extends RadioModule {
    private int[] unitCounts;

    UnitCounts(RobotController rc, int lowestSlot) {
        super(rc, lowestSlot);
    }

    @Override
    public int slotsRequired() {
        // Add 6 just in case ;)
        return NUM_ROBOT_TYPES + 6;
    }

    public void increment(RobotType rtype) {
        final int slot = lowestSlot + rtype.ordinal();
        tx(slot, rx(slot) + 1);
    }

    // Reset all the counters.
    // HQ ONLY.
    public void reset() {
        for (int i = NUM_ROBOT_TYPES-1; i != 0; i--) {
            tx(lowestSlot + i, 0);
        }
    }

    // Read all counters from radio and save to cache.
    // HQ ONLY.
    public void saveToCache() {
        unitCounts = new int[NUM_ROBOT_TYPES];
        for (int i = NUM_ROBOT_TYPES-1; i != 0; i--) {
            unitCounts[i] = rx(lowestSlot + i);
        }
    }

    // Get the current unit count for `rtype` from the cache.
    // Requires that saveToCache() has been called recently.
    // HQ ONLY.
    public int readCached(RobotType rtype) {
        return unitCounts[rtype.ordinal()];
    }
}
