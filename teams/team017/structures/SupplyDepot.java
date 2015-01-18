package team017.structures;

import team017.Structure;
import battlecode.common.RobotController;

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
