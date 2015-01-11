package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class Tank extends Unit {
    Tank(RobotController rc) { super(rc); }


    @Override
    public void run() {

        waitForSupplies();

        // Main loop
        while (true) {
            shootBaddies();

            rf.loadRally(rallyGroup);
            if (rc.isCoreReady()) moveToRallyPoint(rf.getRally(rallyGroup));

            // At 10% health dump supplies
            // Maybe not helpful. Eleminate if we need to optimize.
            if (rc.getHealth() <= rc.getType().maxHealth / 10.0) {
                dumpSuppliesToNeighbor();
            }
            rc.yield();
        }
    }
}
