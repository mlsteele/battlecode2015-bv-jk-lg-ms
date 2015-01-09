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
                    if (rf.requestResupply(TANKFACTORY_RESUPPLY_AMT))
                        incomingResupply = true;
                }
            } else {
                incomingResupply = false;
            }

            if (rc.isCoreReady())
                spawn(TANK);

            if (supply >= initialSupply(TANK))
                supplyNearbyEmpty(null, TANK, initialSupply(TANK));

            rc.yield();
        }
    }

}
