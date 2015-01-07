package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotMinerFactory extends Robot {
    // Given to the miner factory when built.
    public static final int STARTING_SUPPLY = 10000;

    RobotMinerFactory(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "I am a RobotMinerFactory");

        while (true) {
            // Spawn a miner
            // Only if there is enough supply for them.
            if (rc.isCoreReady()) {
                Direction spawnDir = null;
                int supplyAmount = RobotMiner.STARTING_SUPPLY;
                if (rc.getSupplyLevel() > supplyAmount)
                    spawnDir = spawn(MINER);

                // Supply the spawnling.
                if (spawnDir != null) {
                    // Wait one round for the robot to spawn.
                    rc.yield();
                    MapLocation supplyTargetLoc = rc.getLocation().add(spawnDir);

                    try {
                        rc.transferSupplies(supplyAmount, supplyTargetLoc);
                    } catch (GameActionException e) {
                        e.printStackTrace();
                    }
                }
            }

            rc.yield();
        }
    }

}
