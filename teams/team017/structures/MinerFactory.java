package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class MinerFactory extends Robot {
    // Given to the miner factory when built.
    public static final int STARTING_SUPPLY = 10000;

    MinerFactory(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a MinerFactory");

        while (true) {
            if (rc.isCoreReady() && rc.getSupplyLevel() >= Miner.STARTING_SUPPLY)
                spawn(MINER);

            supplyNearbyEmpty(null, MINER, Miner.STARTING_SUPPLY);

            rc.yield();
        }
    }

}
