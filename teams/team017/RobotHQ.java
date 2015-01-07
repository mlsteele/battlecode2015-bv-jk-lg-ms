package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotHQ extends Robot {
    private static final int TIME_TO_ATTACK1 = 500;
    private static final int TIME_TO_ATTACK2 = 1000;

    RobotHQ(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotHQ");

        // Tell all soldiers to rally at our first tower.
        rf.writeRally1(rc.senseTowerLocations()[0]);
        rc.setIndicatorString(1, "set rally1 to " + rf.getRally1());

        while(true) {
            shootBaddies();

            if (Math.abs(Clock.getRoundNum() - TIME_TO_ATTACK1) <= 1) {
                // Rally at half point.
                rf.writeRally1(avgLocations(rc.senseEnemyHQLocation(), rc.senseHQLocation()));
                rc.setIndicatorString(1, "set rally1 to " + rf.getRally1());
            }

            if (Math.abs(Clock.getRoundNum() - TIME_TO_ATTACK2) <= 1) {
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
                        if (rc.getSupplyLevel() > 2*RobotBarracks.STARTING_SUPPLY) {
                            rc.transferSupplies(RobotBeaver.STARTING_SUPPLY + RobotBarracks.STARTING_SUPPLY, supplyTargetLoc);
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

    private MapLocation avgLocations(MapLocation a, MapLocation b) {
        return new MapLocation((a.x + b.x) / 2, (a.y + b.y) / 2);
    }
}