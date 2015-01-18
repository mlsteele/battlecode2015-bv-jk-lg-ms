package team017.structures;

import static battlecode.common.RobotType.*;
import team017.*;
import battlecode.common.*;

public class MinerFactory extends Structure {
    public MinerFactory(RobotController rc) { super(rc); }

    private int[] unitCounts;

    @Override
    public void run() {
        boolean incomingResupply = false;

        while (true) {
            callForHelp();

            // Request resupply when low
            double supply = rc.getSupplyLevel();
            if (Analyze.ON) Analyze.aggregate("mfs_supply", supply);
            if (supply <= Strategy.MINERFACTORY_LOW_SUPPLY) {
                if (!incomingResupply) {
                    if (rf.resupply.request(Strategy.MINERFACTORY_RESUPPLY_AMT))
                        incomingResupply = true;
                }
            } else {
                incomingResupply = false;
            }

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
