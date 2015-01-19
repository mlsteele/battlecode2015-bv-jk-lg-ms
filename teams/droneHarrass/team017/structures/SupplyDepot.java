package droneHarrass.structures;

import droneHarrass.*;
import battlecode.common.*;

public class SupplyDepot extends Structure {
    public SupplyDepot(RobotController rc) { super(rc); }

    @Override
    public void run() {
        while (true) {
            callForHelp();
            rc.yield();
        }
    }

}
