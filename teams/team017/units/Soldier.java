package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class Soldier extends Unit {
    Soldier(RobotController rc) { super(rc); }

    @Override
    public void run() {
        waitForSupplies();

        // Main loop
        while (true) {
            shootBaddies();

            rf.loadRally(rallyGroup);
            if (rc.isCoreReady()) moveToRallyPoint(rf.getRally(rallyGroup));

            rc.yield();
        }
    }
}
