package droneHarrass.structures;

import static battlecode.common.RobotType.*;
import static droneHarrass.Strategy.*;
import droneHarrass.*;
import battlecode.common.*;

public class TankFactory extends Structure {
    public TankFactory(RobotController rc) { super(rc); }

    private int[] unitCounts;

    @Override
    public void run() {
        while (true) {
            callForHelp();

            requestResupplyIfLow(TANKFACTORY_LOW_SUPPLY, TANKFACTORY_RESUPPLY_AMT);

            // Tanks take so long to build that we should build
            // them even if we don't have the supply to fuel them.
            if (rc.isCoreReady() && rf.limitproduction.shouldBuild(TANK))
                spawn(TANK);

            if (rc.getSupplyLevel() < initialSupply(TANK))
                if (Analyze.ON) Analyze.aggregate("tankfactory_supplyblock", 1);
            supplyNearbyEmpty(null, TANK, initialSupply(TANK));

            rc.yield();
        }
    }

}
