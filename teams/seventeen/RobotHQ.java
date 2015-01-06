package miles;

import battlecode.common.*;
import java.util.*;

public class RobotHQ extends Robot {
    RobotHQ(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotHQ");

        while(true) {
            if (rc.isCoreReady()) {
                safeSpawn(Direction.NORTH, RobotType.BEAVER);
            }

            // Supply spawnlings
            try {
                MapLocation supplyTargetLoc = rc.getLocation().add(Direction.NORTH);
                RobotInfo   supplyTarget    = rc.senseRobotAtLocation(supplyTargetLoc);
                if (supplyTarget != null && supplyTarget.supplyLevel < RobotBeaver.STARTING_SUPPLY / 2) {
                    rc.transferSupplies(RobotBeaver.STARTING_SUPPLY, supplyTargetLoc);
                }
            } catch (GameActionException e) { }

            rc.yield();
        }
    }
}
