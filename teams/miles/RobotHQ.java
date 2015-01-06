package miles;

import battlecode.common.*;
import java.util.*;

public class RobotHQ extends Robot {
    RobotHQ(RobotController rc) {
        super(rc);
    }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotHQ");

        while(true) {
            rc.yield();
        }
    }
}
