package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class Barracks extends Structure {
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
                supplyNearbyEmpty(candidates, SOLDIER, Strategy.initialSupply(SOLDIER));
                supplyNearbyEmpty(candidates, BASHER, Strategy.initialSupply(BASHER));
            }

            rc.yield();
        }
    }

    // Spawn a solider or basher if there is enough supply to start them.
    private void spawnCombatant() {
        int supplyAmount;
        if((rand.nextDouble() * 2) <= 1) {
            supplyAmount = Strategy.initialSupply(SOLDIER);
            if (rc.getSupplyLevel() >= supplyAmount)
                spawn(SOLDIER);
        } else {
            supplyAmount = Strategy.initialSupply(BASHER);
            if (rc.getSupplyLevel() >= supplyAmount)
                spawn(BASHER);
        }
    }
}
