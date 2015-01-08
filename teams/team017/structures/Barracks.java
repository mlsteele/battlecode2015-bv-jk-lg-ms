package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class Barracks extends Structure {
    // Given to the barracks when built.
    public static final int STARTING_SUPPLY = 10000;

    Barracks(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a Barracks");

        while (true) {
            if (false) {
                if (rc.isCoreReady()) spawnCombatant();

                // Supply any nearby spawnees that are waiting.
                RobotInfo[] candidates = rc.senseNearbyRobots(
                        GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                        rc.getTeam());
                supplyNearbyEmpty(candidates, SOLDIER, Soldier.STARTING_SUPPLY);
                supplyNearbyEmpty(candidates, BASHER, Basher.STARTING_SUPPLY);
            }

            rc.yield();
        }
    }

    // Spawn a solider or basher if there is enough supply to start them.
    private void spawnCombatant() {
        int supplyAmount;
        if((rand.nextDouble() * 2) <= 1) {
            supplyAmount = Soldier.STARTING_SUPPLY;
            if (rc.getSupplyLevel() >= supplyAmount)
                spawn(SOLDIER);
        } else {
            supplyAmount = Basher.STARTING_SUPPLY;
            if (rc.getSupplyLevel() >= supplyAmount)
                spawn(BASHER);
        }
    }
}
