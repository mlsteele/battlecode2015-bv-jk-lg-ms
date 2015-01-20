package team017.structures;

import team017.*;
import battlecode.common.*;

public class Tower extends Structure {
    public Tower(RobotController rc) { super(rc); }

    @Override
    public void run() {

        // Report ore here
        rf.orelocations.foundSweetOre(rc.getLocation(), (int) rc.senseOre(rc.getLocation()));

        while (true) {
            callForHelp();
            shootBaddies();
            rc.yield();
        }
    }

}
