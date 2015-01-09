package team017;

import battlecode.common.*;
import java.util.*;

public class SupplyDepot extends Structure {
    SupplyDepot(RobotController rc) { super(rc); }

    @Override
    public void run() {
        while (true) {
            rc.yield();
        }
    }


}
