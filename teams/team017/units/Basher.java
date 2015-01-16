package team017;

import battlecode.common.*;
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
            if (rc.isCoreReady()) moveToRallyPoint(rf.rallypoints.get(rallyGroup));

            rc.yield();
        }
    }
}
