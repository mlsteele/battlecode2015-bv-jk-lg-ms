package team017;

import battlecode.common.*;
import java.util.*;

public class SupplyDepot extends Structure {
    SupplyDepot(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a supply depot");

        while (true) {
            rc.yield();
        }
    }


}
