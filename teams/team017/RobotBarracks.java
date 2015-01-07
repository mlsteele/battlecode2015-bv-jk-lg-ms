package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotBarracks extends Robot {
    // Given to the barracks when built.
    public static final int STARTING_SUPPLY = 10000;

    RobotBarracks(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBarracks");

        while (true) {
            if (rc.isCoreReady()) spawnCombatent();

            // Supply any nearby spawnees that are waiting.
            RobotInfo[] candidates = rc.senseNearbyRobots(
                    rc.getLocation(),
                    GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                    rc.getTeam());
            supplyNearby(candidates, SOLDIER, RobotSoldier.STARTING_SUPPLY);
            supplyNearby(candidates, BASHER, RobotBasher.STARTING_SUPPLY);

            rc.yield();
        }
    }

    // Spawn a solider or basher if there is enough supply to start them.
    private void spawnCombatent() {
        if((rand.nextDouble() * 2) <= 1) {
            supplyAmount = RobotSoldier.STARTING_SUPPLY;
            if (rc.getSupplyLevel() > supplyAmount)
                spawn(SOLDIER);
        } else {
            supplyAmount = RobotBasher.STARTING_SUPPLY;
            if (rc.getSupplyLevel() > supplyAmount)
                spawn(BASHER);
        }
    }
}
