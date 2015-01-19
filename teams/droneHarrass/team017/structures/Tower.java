package droneHarrass.structures;

import droneHarrass.*;
import battlecode.common.*;

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
