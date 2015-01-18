package team017.structures;

import team017.*;
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
