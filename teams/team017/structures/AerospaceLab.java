package team017.structures;

import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import team017.*;
import battlecode.common.*;

public class AerospaceLab extends Structure {
    public AerospaceLab(RobotController rc) { super(rc); }

    private int[] unitCounts;

    @Override
    public void run() {
        while (true) {
            callForHelp();

            if (rc.isCoreReady() && rf.limitproduction.shouldBuild(LAUNCHER))
                spawn(LAUNCHER);

            supplyNearbyEmpty(null, LAUNCHER, initialSupply(LAUNCHER));

            rc.yield();
        }
    }

}
