package team017.units;

import team017.*;
import battlecode.common.*;

public class Tank extends Unit {
    public Tank(RobotController rc) { super(rc); }


    @Override
    public void run() {

        waitForSupplies();

        // Main loop
        while (true) {
            shootBaddies();

            if (rc.isCoreReady()) {
                Bugging.target = rf.rallypoints.get(rallyGroup);
                if (Bugging.target != null) {
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
