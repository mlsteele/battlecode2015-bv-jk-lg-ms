package team017.structures;

import static battlecode.common.RobotType.*;
import team017.*;
import battlecode.common.*;

public class Barracks extends Structure {
    public Barracks(RobotController rc) { super(rc); }

    @Override
    public void run() {
        while (true) {
            callForHelp();

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
        if ((rand.nextDouble() * 2) <= 1) {
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
