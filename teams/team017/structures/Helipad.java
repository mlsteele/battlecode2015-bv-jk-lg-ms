package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class Helipad extends Structure {
    Helipad(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a Helipad");

        while (true) {
            if (rc.isCoreReady() && rc.getSupplyLevel() >= Strategy.initialSupply(DRONE))
                spawn(DRONE);

            supplyNearbyEmpty(null, DRONE, Strategy.initialSupply(DRONE));

            rc.yield();
        }
    }

}
