package team017.units;

import team017.Unit;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Soldier extends Unit {
    public Soldier(RobotController rc) { super(rc); }

    @Override
    public void run() {
        waitForSupplies();

        // Main loop
        while (true) {
            shootBaddies();

            if (rc.isCoreReady()) {
                MapLocation target = rf.rallypoints.get(rallyGroup);
                if (target != null) {
                    moveToRallyPoint(target);
                }
            }

            rc.yield();
        }
    }
}
