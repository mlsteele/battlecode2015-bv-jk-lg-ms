package team017;

import team017.structures.*;
import team017.units.*;
import battlecode.common.*;

// Entry point for robots.
// Dispatches to one of the Robot classes.
public class RobotPlayer {
    public static void run(RobotController rc) {
        Robot r = resolveRobotType(rc);

        if (r != null) {
            // Should never return.
            r.run();
        } else {
            // Unimplemented or unknown robot type
            rc.setIndicatorString(0, "Having existential crisis.");
            while (true) {
                rc.yield();
            }
        }
    }

    private static Robot resolveRobotType(RobotController rc) {
        switch (rc.getType()) {
            case HQ: return new Headquarters(rc);
            case TOWER: return new Tower(rc);
            case BEAVER: return new Beaver(rc);
            case BARRACKS: return new Barracks(rc);
            case SOLDIER: return new Soldier(rc);
            case BASHER: return new Basher(rc);
            case MINER: return new Miner(rc);
            case MINERFACTORY: return new MinerFactory(rc);
            case TANK: return new Tank(rc);
            case TANKFACTORY: return new TankFactory(rc);
            case HELIPAD: return new Helipad(rc);
            case DRONE: return new Drone(rc);
            case SUPPLYDEPOT: return new SupplyDepot(rc);
            default: return null;
        }
    }
}
