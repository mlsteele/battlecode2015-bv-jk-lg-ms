package seventeen;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotBarracks extends Robot {
    RobotBarracks(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBarracks");

        while (true) {
            if (rc.isCoreReady()) {
                if((rand.nextDouble() * 2) <= 1) {
                    spawn(SOLDIER);
                } else {
                    spawn(BASHER);
                }
            }

            rc.yield();
        }
    }

    private void spawn(RobotType rtype) {
        Direction dir = randomDirection();
        if (rc.canSpawn(dir, rtype)) {
            try {
                rc.spawn(dir, rtype);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }
}
