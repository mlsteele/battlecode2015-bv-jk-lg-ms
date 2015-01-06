package miles;

import battlecode.common.*;
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

        while(true) {
            rc.setIndicatorString(2, "supply: " + rc.getSupplyLevel());

            if (rc.isCoreReady()) {
                safeMove(Direction.NORTH);
            }

            rc.yield();
        }
    }
}
