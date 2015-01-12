package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class MinerFactory extends Structure {
    MinerFactory(RobotController rc) { super(rc); }

    private int[] unitCount;

    @Override
    public void run() {
        boolean incomingResupply = false;

        while (true) {

            // Request resupply when low
            double supply = rc.getSupplyLevel();
            if (Analyze.ON) Analyze.aggregate("mfs_supply", supply);
            if (supply <= MINERFACTORY_LOW_SUPPLY) {
                if (!incomingResupply) {
                    if (rf.requestResupply(MINERFACTORY_RESUPPLY_AMT))
                        incomingResupply = true;
                }
            } else {
                incomingResupply = false;
            }

            if (rc.isCoreReady() && rc.getSupplyLevel() >= Strategy.initialSupply(MINER)) {
                unitCount = getUnitCount();
                if(unitCount[MINER.ordinal()] < rf.checkXUnits(MINER)) {
                        spawn(MINER);
                }
            }

            supplyNearbyEmpty(null, MINER, Strategy.initialSupply(MINER));

            rc.yield();
        }
    }

}
