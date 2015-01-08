package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class TankFactory extends Structure {
    TankFactory(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a TankFactory");

        while (true) {
            if (rc.isCoreReady() && rc.getSupplyLevel() >= Strategy.initialSupply(TANK))
                spawn(TANK);

            supplyNearbyEmpty(null, TANK, Strategy.initialSupply(TANK));

            rc.yield();
        }
    }

}
