package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class Basher extends Unit {
    Basher(RobotController rc) { super(rc); }

    @Override
    public void run() {
        waitForSupplies();

        // Main loop
        while (true) {
            rf.loadRally(rallyGroup);
            if (rc.isCoreReady()) moveToRallyPoint(rf.getRally(rallyGroup));

            rc.yield();
        }
    }
}
