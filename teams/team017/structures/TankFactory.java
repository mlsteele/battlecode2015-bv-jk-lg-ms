package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class TankFactory extends Structure {
    TankFactory(RobotController rc) { super(rc); }

    @Override
    public void run() {
        boolean incomingResupply = false;

        while (true) {
            double supply = rc.getSupplyLevel();
            if (supply <= TANKFACTORY_LOW_SUPPLY) {
                if (!incomingResupply) {
                    if (rf.requestResupply(TANKFACTORY_RESUPPLY_AMT)) {
                        incomingResupply = true;
                    }
                }
            } else {
                incomingResupply = false;
            }

            // Tanks take so long to build that we should build
            // them even if we don't have the supply to fuel them.
            if (rc.isCoreReady())
                spawn(TANK);

            if (supply < initialSupply(TANK))
                Analyze.aggregate("tankfactory_supplyblock", 1);
            supplyNearbyEmpty(null, TANK, initialSupply(TANK));

            rc.yield();
        }
    }

}
