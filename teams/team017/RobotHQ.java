package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

public class RobotHQ extends Robot {
    private static final int TIME_TO_ATTACK1 = 500;
    private static final int TIME_TO_ATTACK2 = 1000;
    private static final int MAX_BEAVER = 10;

    // Supply-based order codes that HQ gives to beavers
    public static final int ORDER_NONE = 0;
    public static final int ORDER_BARRACKS = 1;
    public static final int ORDER_MINERFACTORY = 2;

    private int spawned_beavers = 0;
    private boolean beaver_mining_spawned = false;

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
            if (rc.isCoreReady() && rc.getSupplyLevel() > RobotBeaver.STARTING_SUPPLY &&
                    spawned_beavers < MAX_BEAVER) {
                Direction spawnDir = spawn(BEAVER);
                spawned_beavers++;

                // Supply spawnling
                if (spawnDir != null) {
                    // Wait one round for the robot to spawn.
                    rc.yield();
                    MapLocation supplyTargetLoc = rc.getLocation().add(spawnDir);


                    try {
                        // If it would be reasonable to supply a barracks, order one built.
                        if (rc.getSupplyLevel() > RobotMinerFactory.STARTING_SUPPLY &&
                                beaver_mining_spawned == false) {
                            //rc.setIndicatorString(0, "Spawning a miner beaver");
                            beaver_mining_spawned = true;
                            rc.transferSupplies(
                                    RobotBeaver.STARTING_SUPPLY + RobotMinerFactory.STARTING_SUPPLY + ORDER_MINERFACTORY,
                                    supplyTargetLoc);
                        } else if (rc.getSupplyLevel() > RobotBarracks.STARTING_SUPPLY) {
                            rc.transferSupplies(
                                    RobotBeaver.STARTING_SUPPLY + RobotBarracks.STARTING_SUPPLY + ORDER_BARRACKS,
                                    supplyTargetLoc);
                        } else {
                            rc.transferSupplies(RobotBeaver.STARTING_SUPPLY + ORDER_NONE, supplyTargetLoc);
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
