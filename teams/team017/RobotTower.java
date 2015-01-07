package team017;

import battlecode.common.*;
import java.util.*;

public class RobotTower extends Robot {
    RobotTower(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotTower");

        while (true) {
            shootBaddies();

            rc.yield();
        }
    }


}
