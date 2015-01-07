package seventeen;

import battlecode.common.*;
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

            if (rc.isCoreReady()) {
                safeSpawn(Direction.NORTH, RobotType.BEAVER);
            }

            // Supply spawnlings
            try {
                MapLocation supplyTargetLoc = rc.getLocation().add(Direction.NORTH);
                RobotInfo   supplyTarget    = rc.senseRobotAtLocation(supplyTargetLoc);
                if (supplyTarget != null && supplyTarget.supplyLevel < RobotBeaver.STARTING_SUPPLY / 2) {
                    if (rc.getSupplyLevel() > 2*RobotBeaver.SUPPLY_FOR_BARRACKS)
                        rc.transferSupplies(RobotBeaver.STARTING_SUPPLY + RobotBeaver.SUPPLY_FOR_BARRACKS, supplyTargetLoc);
                    else
                        rc.transferSupplies(RobotBeaver.STARTING_SUPPLY, supplyTargetLoc);
                }
            } catch (GameActionException e) { }

            rc.yield();
        }
    }
}
