package team017.structures;

import static battlecode.common.RobotType.*;
import team017.*;
import static team017.Strategy.*;
import battlecode.common.*;

public class MinerFactory extends Structure {
    public MinerFactory(RobotController rc) { super(rc); }

    private int[] unitCounts;

    @Override
    public void run() {
        while (true) {
            callForHelp();

            requestResupplyIfLow(
                    Strategy.initialSupply(MINER),
                    2*Strategy.initialSupply(MINER));

            if (rc.isCoreReady() && rc.getSupplyLevel() >= Strategy.initialSupply(MINER)) {
                unitCounts = getUnitCounts();
                if(unitCounts[MINER.ordinal()] < rf.xunits.get(MINER)) {
                    spawn(MINER);
                }
            }

            supplyNearbyEmpty(null, MINER, Strategy.initialSupply(MINER));

            rc.yield();
        }
    }

}
