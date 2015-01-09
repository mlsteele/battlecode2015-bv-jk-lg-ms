package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

// Strategy constants
public enum Strategy {;
    // Task codes that HQ gives to beavers.
    public static final int TASK_REQUESTING_TASK = -1;
    public static final int TASK_NONE = 0;
    public static final int TASK_BARRACKS = 1;
    public static final int TASK_MINERFACTORY = 2;
    public static final int TASK_TANKFACTORY = 3;
    public static final int TASK_HELIPAD = 4;
    public static final int TASK_SUPPLYDEPOT = 5;
    public static final int TASK_RESUPPLY_TANKFACTORY = 6;

    // Miners don't request supply if team has excess ore.
    public static final int TEAM_LOW_ORE = 1500;

    // Return to HQ to resupply if below this level.
    public static final int MINER_LOW_SUPPLY = 75;
    // Get this much from the HQ.
    public static final int MINER_RESUPPLY_FROM_HQ = 2000;

    // Call for resupply at this point.
    public static final int TANKFACTORY_LOW_SUPPLY = initialSupply(TANK);
    public static final int TANKFACTORY_RESUPPLY_AMT = 10 * initialSupply(TANK);

    public static final int RALLY_ARMY = 0;

    public static int taskSupply(int task) {
        switch(task) {
            case TASK_NONE:              return initialSupply(BEAVER);
            case TASK_BARRACKS:          return initialSupply(BEAVER) + initialSupply(BARRACKS);
            case TASK_MINERFACTORY:      return initialSupply(BEAVER) + initialSupply(MINERFACTORY);
            case TASK_TANKFACTORY:       return initialSupply(BEAVER) + initialSupply(TANKFACTORY);
            case TASK_HELIPAD:           return initialSupply(BEAVER) + initialSupply(HELIPAD);
            case TASK_SUPPLYDEPOT:       return initialSupply(BEAVER) + initialSupply(SUPPLYDEPOT);
            case TASK_RESUPPLY_TANKFACTORY: return initialSupply(BEAVER) + TANKFACTORY_RESUPPLY_AMT;
            default:                     throw new NotImplementedException();
        }
    }

    public static int initialSupply(RobotType rtype) {
        switch (rtype) {
            case BEAVER:              return 1000;

            case MINERFACTORY:        return 10 * initialSupply(MINER);
            case MINER:               return 1000;

            case BARRACKS:            return 10 * initialSupply(SOLDIER);
            case SOLDIER:             return 1000;
            case BASHER:              return 1000;

            case HELIPAD:             return 20 * initialSupply(DRONE);
            case DRONE:               return 1000;

            case TANKFACTORY:         return 6 * initialSupply(TANK);
            case TANK:                return 4000;

            case SUPPLYDEPOT:         return 0;

            case AEROSPACELAB:        throw new NotImplementedException();
            case COMMANDER:           throw new NotImplementedException();
            case COMPUTER:            throw new NotImplementedException();
            case HANDWASHSTATION:     throw new NotImplementedException();
            case HQ:                  throw new NotImplementedException();
            case LAUNCHER:            throw new NotImplementedException();
            case MISSILE:             throw new NotImplementedException();
            case TECHNOLOGYINSTITUTE: throw new NotImplementedException();
            case TOWER:               throw new NotImplementedException();
            case TRAININGFIELD:       throw new NotImplementedException();
            default:                  throw new NotImplementedException();
        }
    }
}
