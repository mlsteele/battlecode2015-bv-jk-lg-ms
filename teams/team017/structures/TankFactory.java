package team017.structures;

import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import team017.*;
import battlecode.common.*;

public class TankFactory extends Structure {
    public TankFactory(RobotController rc) { super(rc); }

    private int[] unitCounts;

    @Override
    public void run() {
        boolean incomingResupply = false;

        while (true) {
            callForHelp();

            double supply = rc.getSupplyLevel();
            if (Analyze.ON) Analyze.aggregate("tfs_supply", supply);
            if (supply <= TANKFACTORY_LOW_SUPPLY) {
                if (!incomingResupply) {
                    if (rf.resupply.request(TANKFACTORY_RESUPPLY_AMT)) {
                        incomingResupply = true;
                    }
                }
            } else {
                incomingResupply = false;
            }

            // Tanks take so long to build that we should build
            // them even if we don't have the supply to fuel them.
            if (rc.isCoreReady() && rf.limitproduction.shouldBuild(TANK))
                spawn(TANK);

            if (supply < initialSupply(TANK))
                if (Analyze.ON) Analyze.aggregate("tankfactory_supplyblock", 1);
            supplyNearbyEmpty(null, TANK, initialSupply(TANK));

            rc.yield();
        }
    }

}
