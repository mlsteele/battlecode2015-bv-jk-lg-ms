package droneHarrass.structures;

import static battlecode.common.RobotType.*;
import droneHarrass.*;
import static droneHarrass.Strategy.*;
import battlecode.common.*;

public class MinerFactory extends Structure {
    public MinerFactory(RobotController rc) { super(rc); }

    private int[] unitCounts;

    @Override
    public void run() {
        while (true) {
            callForHelp();

            requestResupplyIfLow(MINERFACTORY_LOW_SUPPLY, MINERFACTORY_RESUPPLY_AMT);

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
