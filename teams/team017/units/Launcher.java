package team017.units;

import team017.*;
import battlecode.common.*;
import static battlecode.common.Direction.*;
import static team017.Strategy.*;

public class Launcher extends Unit {
    public Launcher(RobotController rc) { super(rc); }

    @Override
    public void run() {
        waitForSupplies();

        // Main loop
        while (true) {
            MapLocation missileTarget = rc.getLocation().add(-5, 5);
            launchMissile(missileTarget);

            if (rc.isCoreReady()) {
                MapLocation target = rf.rallypoints.get(rallyGroup);
                if (target != null) {
                    moveToRallyPoint(target);
                }
            }

            rc.yield();
        }
    }

    private void launchMissile(MapLocation target) {
        if (rc.getMissileCount() == 0) return;
        Direction dir = rc.getLocation().directionTo(target);

        // TODO(miles): don't give up this easily.
        if (!rc.canLaunch(dir)) return;

        try {
            rc.launchMissile(dir);
            rf.rallypoints.set(RALLY_MISSILE_STRIKE, target);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
