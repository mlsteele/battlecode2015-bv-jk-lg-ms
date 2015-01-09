package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;

import static team017.Strategy.*;

import java.util.*;

public class TankFactory extends Structure {
    TankFactory(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a TankFactory");

        boolean incomingResupply = false;

        while (true) {

            if (rc.getSupplyLevel() <= TANKFACTORY_LOW_SUPPLY) {
                if (!incomingResupply) {
                    rc.setIndicatorString(1, "Requesting Supply");
                    if (rf.requestResupply(TANKFACTORY_RESUPPLY_AMT))
                        incomingResupply = true;
                }
            } else {
                rc.setIndicatorString(1, "Not requesting supply");
                incomingResupply = false;
                if (rc.isCoreReady())
                    spawn(TANK);

                supplyNearbyEmpty(null, TANK, initialSupply(TANK));
            }


            rc.yield();
        }
    }

}
