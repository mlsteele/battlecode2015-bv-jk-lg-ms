package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class Drone extends Unit {
    Drone(RobotController rc) { super(rc); }

    @Override
    public void run() {
        waitForSupplies();

        // Main loop
        while (true) {
            shootBaddies();

            rf.loadRally(RALLY_ARMY);
            if (rc.isCoreReady()) moveToRallyPoint(rf.getRally(RALLY_ARMY));

            rc.yield();
        }
    }
}
