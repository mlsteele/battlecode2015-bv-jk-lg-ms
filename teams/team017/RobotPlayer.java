package team017;

import team017.structures.*;
import team017.units.*;
import battlecode.common.*;

// Entry point for robots.
// Dispatches to one of the Robot classes.
public class RobotPlayer {
    public static void run(RobotController rc) {
        S.rc = rc;

        switch (rc.getType()) {
            case HQ:                  new Headquarters(rc)        .run (); break ;
            case TOWER:               new Tower(rc)               .run (); break ;

            case MINERFACTORY:        new MinerFactory(rc)        .run (); break ;
            case SUPPLYDEPOT:         new SupplyDepot(rc)         .run (); break ;
            case BARRACKS:            new Barracks(rc)            .run (); break ;
            case HELIPAD:             new Helipad(rc)             .run (); break ;
            case TANKFACTORY:         new TankFactory(rc)         .run (); break ;
            case AEROSPACELAB:        new AerospaceLab(rc)        .run (); break ;
            case TECHNOLOGYINSTITUTE: new TechnologyInstitute(rc) .run (); break ;
            case TRAININGFIELD:       new TrainingField(rc)       .run (); break ;

            case BEAVER:       new Beaver(rc)       .run (); break ;
            case MINER:        new Miner(rc)        .run (); break ;
            case SOLDIER:      new Soldier(rc)      .run (); break ;
            case BASHER:       new Basher(rc)       .run (); break ;
            case DRONE:        new Drone(rc)        .run (); break ;
            case TANK:         new Tank(rc)         .run (); break ;
            case LAUNCHER:     new Launcher(rc)     .run (); break ;
            case COMMANDER:    new Commander(rc)    .run (); break ;

            case MISSILE:      Missile.run(rc); break ;

            default:
                // Unimplemented or unknown robot type
                rc.setIndicatorString(0, "Having existential crisis.");
                while (true) {
                    rc.yield();
                }
        }
    }
}
