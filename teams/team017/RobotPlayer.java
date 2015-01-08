package team017;

import battlecode.common.*;
import java.util.*;

// Entry point for robots.
public class RobotPlayer {
    public static void run(RobotController rc) {
        Robot r;

        // Dispatch to one of the Robot* classes.
        // Should never return.
        switch (rc.getType()) {
            case HQ:
                r = new HQ(rc);
                r.run();
                break;
            case TOWER:
                r = new Tower(rc);
                r.run();
                break;
            case BEAVER:
                r = new Beaver(rc);
                r.run();
                break;
            case BARRACKS:
                r = new Barracks(rc);
                r.run();
                break;
            case SOLDIER:
                r = new Soldier(rc);
                r.run();
                break;
            case BASHER:
                r = new Basher(rc);
                r.run();
                break;
            case MINER:
                r = new Miner(rc);
                r.run();
                break;
            case MINERFACTORY:
                r = new MinerFactory(rc);
                r.run();
                break;
            case TANK:
                r = new Tank(rc);
                r.run();
                break;
            case TANKFACTORY:
                r = new Tank(rc);
                r.run();
                break;
            default:
                // Unimplemented robot type
                rc.setIndicatorString(0, "having existential crisis.");
                while (true) {
                    rc.yield();
                }
        }
    }
}
