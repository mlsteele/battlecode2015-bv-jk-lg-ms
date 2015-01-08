package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

// Strategy constants
public enum Strategy {;
    // Task codes that HQ gives to beavers.
    public static final int TASK_NONE = 0;
    public static final int TASK_BARRACKS = 1;
    public static final int TASK_MINERFACTORY = 2;
    public static final int TASK_TANKFACTORY = 3;
    public static final int TASK_HELIPAD = 4;

    public static int initialSupply(RobotType rtype) {
        switch (rtype) {
            case BEAVER:              return 1000;

            case MINERFACTORY:        return 10 * initialSupply(MINER);
            case MINER:               return 3000;

            case BARRACKS:            return 10 * initialSupply(SOLDIER);
            case SOLDIER:             return 1000;
            case BASHER:              return 1000;

            case HELIPAD:             return 20 * initialSupply(DRONE);
            case DRONE:               return 1000;

            case TANKFACTORY:         return 9 * initialSupply(TANK);
            case TANK:                return 4000;

            case AEROSPACELAB:        throw new NotImplementedException();
            case COMMANDER:           throw new NotImplementedException();
            case COMPUTER:            throw new NotImplementedException();
            case HANDWASHSTATION:     throw new NotImplementedException();
            case HQ:                  throw new NotImplementedException();
            case LAUNCHER:            throw new NotImplementedException();
            case MISSILE:             throw new NotImplementedException();
            case SUPPLYDEPOT:         throw new NotImplementedException();
            case TECHNOLOGYINSTITUTE: throw new NotImplementedException();
            case TOWER:               throw new NotImplementedException();
            case TRAININGFIELD:       throw new NotImplementedException();
            default:                  throw new NotImplementedException();
        }
    }
}
