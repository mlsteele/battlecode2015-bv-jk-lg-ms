package team017.radio;

import battlecode.common.*;

public class OreLocations extends RadioModule {
    
    MapLocation enemyHQ = rc.senseEnemyHQLocation();
    MapLocation hQ = rc.senseHQLocation();

    OreLocations(RobotController rc, int lowestSlot) {
        super(rc, lowestSlot);
    }

    @Override
    public int slotsRequired() {
        return 1;
    }
    
    /** 
     * If you find some sweet ore, say so here.
     * If your ore is deemed to indeed be pretty sweet, the radio will be updated.
     * Criteria for sweet ore:
     * - amount > current saved amount
     * - loc is deemed safe for mining (closer to HQ than enemyHQ)
     * 
     * Returns the final sweet amount of ore
     */
    public int foundSweetOre(MapLocation loc, int amount) {
        //System.out.println("beep " + amount);
        int currentBestOre = getAmount();
        if (loc.distanceSquaredTo(enemyHQ) < loc.distanceSquaredTo(hQ))  return currentBestOre;
        if (amount < currentBestOre) return currentBestOre;
        if (amount > 0xFFFF) amount = 0xFFFF; // don't overrun your space

        tx(lowestSlot, (encodeLocation(loc) << 16) | amount);
        return amount;
    }

    public int getAmount() {
        return rx(lowestSlot) & 0xFFFF;
    }
    
    public MapLocation getLocation() {
        return decodeLocation(rx(lowestSlot) >>> 16);
    }

}
