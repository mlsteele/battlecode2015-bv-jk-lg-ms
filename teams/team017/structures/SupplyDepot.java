package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
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
