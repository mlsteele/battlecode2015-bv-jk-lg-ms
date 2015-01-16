package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class Helipad extends Structure {
    Helipad(RobotController rc) { super(rc); }

    private int[] unitCount;

    @Override
    public void run() {
        while (true) {
            rc.setIndicatorString(0, null);

            if (rc.isCoreReady() && rc.getSupplyLevel() >= Strategy.initialSupply(DRONE)) {
                unitCount = getUnitCounts();
                int unitsExist = unitCount[DRONE.ordinal()];
                int unitsRequested = rf.xunits.get(DRONE);
                rc.setIndicatorString(0, "units "+unitsExist+"/"+unitsRequested);
                if (unitsExist < unitsRequested)
                    spawn(DRONE);
            }

            supplyNearbyEmpty(null, DRONE, Strategy.initialSupply(DRONE));

            rc.yield();
        }
    }

}
