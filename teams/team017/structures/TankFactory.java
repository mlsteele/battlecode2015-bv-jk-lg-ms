package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class TankFactory extends Structure {
    // Given to the miner factory when built.
    public static final int STARTING_SUPPLY = 10000;

    TankFactory(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a TankFactory");

        while (true) {
            if (rc.isCoreReady() && rc.getSupplyLevel() > Tank.STARTING_SUPPLY) spawn(TANK);

            // Supply any nearby spawnees that are waiting.
            supplyNearbyEmpty(null, TANK, Tank.STARTING_SUPPLY);

            rc.yield();
        }
    }

}
