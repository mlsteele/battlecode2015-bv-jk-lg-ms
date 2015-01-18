package team017;

import team017.structures.Barracks;
import team017.structures.Headquarters;
import team017.structures.Helipad;
import team017.structures.MinerFactory;
import team017.structures.TankFactory;
import team017.structures.Tower;
import team017.units.Basher;
import team017.units.Beaver;
import team017.units.Drone;
import team017.units.Miner;
import team017.units.Soldier;
import team017.units.Tank;
import battlecode.common.RobotController;

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
