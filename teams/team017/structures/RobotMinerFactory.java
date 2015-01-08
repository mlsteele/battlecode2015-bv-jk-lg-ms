package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotMinerFactory extends Robot {
    // Given to the miner factory when built.
    public static final int STARTING_SUPPLY = 10000;

    RobotMinerFactory(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a RobotMinerFactory");

        while (true) {
            if (rc.isCoreReady() && rc.getSupplyLevel() >= RobotMiner.STARTING_SUPPLY)
                spawn(MINER);

            supplyNearbyEmpty(null, MINER, RobotMiner.STARTING_SUPPLY);

            rc.yield();
        }
    }

}
