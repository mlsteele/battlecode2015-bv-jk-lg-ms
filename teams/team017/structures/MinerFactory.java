package team017;

import battlecode.common.*;
import battlecode.common.GameActionException;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.util.*;

public class MinerFactory extends Structure {
    MinerFactory(RobotController rc) { super(rc); }

    @Override
    public void run() {
        while (true) {
            if (rc.isCoreReady() && rc.getSupplyLevel() >= Strategy.initialSupply(MINER))
                spawn(MINER);

            supplyNearbyEmpty(null, MINER, Strategy.initialSupply(MINER));

            rc.yield();
        }
    }

}
