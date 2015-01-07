package seventeen;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotHQ extends Robot {
    private static final int TIME_TO_ATTACK = 500;

    RobotHQ(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotHQ");

        // Tell all soldiers to rally at our first tower.
        rf.writeRally1(rc.senseTowerLocations()[0]);
        rc.setIndicatorString(1, "set rally1 to " + rf.getRally1());

        while(true) {
            shootBaddies();

            if (Math.abs(Clock.getRoundNum() - TIME_TO_ATTACK) <= 1) {
                // Rally at the enemy HQ
                rf.writeRally1(rc.senseEnemyHQLocation());
                rc.setIndicatorString(1, "set rally1 to " + rf.getRally1());
            }

            // Spawn a beaver.
            if (rc.isCoreReady() && rc.getSupplyLevel() > RobotBeaver.STARTING_SUPPLY) {
                Direction spawnDir = spawn(BEAVER);
                // Supply spawnling
                if (spawnDir != null) {
                    // Wait one round for the robot to spawn.
                    rc.yield();
                    MapLocation supplyTargetLoc = rc.getLocation().add(spawnDir);

                    try {
                        // If it would be reasonable to supply a barracks, order one built.
                        if (rc.getSupplyLevel() > 2*RobotBeaver.SUPPLY_FOR_BARRACKS) {
                            rc.transferSupplies(RobotBeaver.STARTING_SUPPLY + RobotBeaver.SUPPLY_FOR_BARRACKS, supplyTargetLoc);
                        } else {
                            rc.transferSupplies(RobotBeaver.STARTING_SUPPLY, supplyTargetLoc);
                        }
                    } catch (GameActionException e) {
                        e.printStackTrace();
                    }
                }
            }

            rc.yield();
        }
    }
}
