package team017.structures;

import team017.Structure;
import battlecode.common.RobotController;

public class Tower extends Structure {
    public Tower(RobotController rc) { super(rc); }

    @Override
    public void run() {
        while (true) {
            callForHelp();
            shootBaddies();
            rc.yield();
        }
    }


}
