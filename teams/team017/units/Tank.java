package team017.units;

import java.util.*;
import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import team017.*;
import team017.radio.*;
import team017.structures.*;
import team017.units.*;
import static team017.Strategy.*;

public class Tank extends Unit {
    public Tank(RobotController rc) { super(rc); }


    @Override
    public void run() {

        waitForSupplies();

        // Main loop
        while (true) {
            shootBaddies();

            if (rc.isCoreReady()) {
                MapLocation target = rf.rallypoints.get(rallyGroup);
                Bugging.setParams(
                        target,
                        MOVEMENT_CLUMP_DEFAULT, false);
                if (target != null) {
                    Bugging.move();
                }
            }

            // At 10% health dump supplies
            // Maybe not helpful. Eleminate if we need to optimize.
            if (rc.getHealth() <= rc.getType().maxHealth / 10.0) {
                dumpSuppliesToNeighbor();
            }
            rc.yield();
        }
    }
}
