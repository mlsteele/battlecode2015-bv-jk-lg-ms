package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class MinerFactory extends Structure {
    MinerFactory(RobotController rc) { super(rc); }

    int[] unitCount;

    @Override
    public void run() {
        while (true) {
            unitCount = updateUnitCount();

            if(unitCount[RobotType.MINER.ordinal()] < rf.checkXUnits(RobotType.MINER)) {
                if (rc.isCoreReady() && rc.getSupplyLevel() >= Strategy.initialSupply(MINER))
                    spawn(MINER);
            }

            supplyNearbyEmpty(null, MINER, Strategy.initialSupply(MINER));

            rc.yield();
        }
    }

}
