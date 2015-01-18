package team017.units;

import team017.*;
import battlecode.common.*;

public class Launcher extends Unit {
    public Launcher(RobotController rc) { super(rc); }

    @Override
    public void run() {

        waitForSupplies();

        // Main loop
        while (true) {
            if (rc.isCoreReady()) {
                MapLocation target = rf.rallypoints.get(rallyGroup);
                if (target != null) {
                    moveToRallyPoint(target);
                }
            }

            rc.yield();
        }
    }
}