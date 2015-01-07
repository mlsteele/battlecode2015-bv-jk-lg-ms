package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotTankFactory extends Robot {
    // Given to the miner factory when built.
    public static final int STARTING_SUPPLY = 10000;

    RobotTankFactory(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a RobotTankFactory");

        while (true) {
            rc.yield();
        }
    }

}
