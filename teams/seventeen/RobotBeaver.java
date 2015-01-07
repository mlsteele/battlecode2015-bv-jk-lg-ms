package seventeen;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotBeaver extends Robot {
    RobotBeaver(RobotController rc) { super(rc); }

    public static final int STARTING_SUPPLY = 1000;

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBeaver");

        // Wait for supplies
        while (rc.getSupplyLevel() <= STARTING_SUPPLY / 2) {
            rc.yield();
        }

        // Main loop
        while(true) {
            rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

            if (rc.isCoreReady()) buildBarracks();
            if (rc.isCoreReady()) wander();

            rc.yield();
        }
    }

    private void buildBarracks() {
        Direction dir = randomDirection();
        if (rc.canBuild(dir, BARRACKS)) {
            try {
                rc.build(dir, BARRACKS);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }
}
