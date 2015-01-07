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
                r = new RobotHQ(rc);
                r.run();
                break;
            case TOWER:
                r = new RobotTower(rc);
                r.run();
                break;
            case BEAVER:
                r = new RobotBeaver(rc);
                r.run();
                break;
            case BARRACKS:
                r = new RobotBarracks(rc);
                r.run();
                break;
            case SOLDIER:
                r = new RobotSoldier(rc);
                r.run();
                break;
            case BASHER:
                r = new RobotBasher(rc);
                r.run();
                break;
            case MINER:
                r = new RobotMiner(rc);
                r.run();
                break;
            case MINERFACTORY:
                r = new RobotMinerFactory(rc);
                r.run();
                break;
            case TANK:
                r = new RobotTank(rc);
                r.run();
                break;
            case TANKFACTORY:
                r = new RobotTank(rc);
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
