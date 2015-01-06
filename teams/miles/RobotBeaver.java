package miles;

import battlecode.common.*;
import java.util.*;

public class RobotBeaver extends Robot {
    RobotBeaver(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBeaver");

        while(true) {
            if (rc.isCoreReady()) {
                safeMove(Direction.NORTH);
            }

            rc.yield();
        }
    }
}
