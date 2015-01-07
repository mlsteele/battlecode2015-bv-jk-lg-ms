package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotBarracks extends Robot {
    // Given to the barracks when built.
    public static final int STARTING_SUPPLY = 10000;

    RobotBarracks(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotBarracks");

        while (true) {
            // Spawn a combatent
            // Only if there is enough supply for them.
            if (rc.isCoreReady()) {
                Direction spawnDir = null;
                int supplyAmount;
                if((rand.nextDouble() * 2) <= 1) {
                    supplyAmount = RobotSoldier.STARTING_SUPPLY;
                    if (rc.getSupplyLevel() > supplyAmount)
                        spawnDir = spawn(SOLDIER);
                } else {
                    supplyAmount = RobotBasher.STARTING_SUPPLY;
                    if (rc.getSupplyLevel() > supplyAmount)
                        spawnDir = spawn(BASHER);
                }

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
