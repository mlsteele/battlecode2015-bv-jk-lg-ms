package team017.structures;

import static battlecode.common.RobotType.*;
import team017.*;
import battlecode.common.*;

public class Helipad extends Structure {
    public Helipad(RobotController rc) { super(rc); }

    private int[] unitCounts;

    @Override
    public void run() {
        while (true) {
            rc.setIndicatorString(0, null);

            callForHelp();

            if (rc.isCoreReady() && rc.getSupplyLevel() >= Strategy.initialSupply(DRONE)) {
                unitCounts = getUnitCounts();
                int unitsExist = unitCounts[DRONE.ordinal()];
                int unitsRequested = rf.xunits.get(DRONE);
                rc.setIndicatorString(0, "units "+unitsExist+"/"+unitsRequested);
                if (unitsExist < unitsRequested || rf.limitproduction.shouldBuild(DRONE))
                    spawn(DRONE);
            }

            supplyNearbyEmpty(null, DRONE, Strategy.initialSupply(DRONE));

            rc.yield();
        }
    }

}
