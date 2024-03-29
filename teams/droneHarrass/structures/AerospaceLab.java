package droneHarrass.structures;

import static battlecode.common.RobotType.*;
import static droneHarrass.Strategy.*;
import droneHarrass.*;
import battlecode.common.*;

public class AerospaceLab extends Structure {
    public AerospaceLab(RobotController rc) { super(rc); }

    private int[] unitCounts;

    @Override
    public void run() {
        while (true) {
            callForHelp();

            requestResupplyIfLow(Strategy.initialSupply(LAUNCHER), Strategy.initialSupply(AEROSPACELAB));

            // Launchers take so long to build that we should build
            // them even if we don't have the supply to fuel them.
            if (rc.isCoreReady() && rf.limitproduction.shouldBuild(LAUNCHER))
                spawn(LAUNCHER);

            supplyNearbyEmpty(null, LAUNCHER, initialSupply(LAUNCHER));

            rc.yield();
        }
    }

}
