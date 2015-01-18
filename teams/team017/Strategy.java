package team017;

import static battlecode.common.RobotType.*;
import battlecode.common.*;

// Strategy constants
public enum Strategy {;
    public static final int RALLY_GROUP_1 = 0;
    public static final int RALLY_GROUP_2 = 1;
    // Group responsible for helping defenseless robots under duress.
    public static final int RALLY_HELP_DEFEND = 2;
    // Location for missiles to target, not really a group.
    public static final int RALLY_MISSILE_STRIKE = 3;

    // How many beavers to maintain for tasks.
    public static final int BEAVER_POOL_SIZE = 4;

    // Miners don't request supply if team has excess ore.
    public static final int TEAM_LOW_ORE = 1500;

    // Return to HQ to resupply if below this level.
    // Call for resupply at this point (mining factory)
    public static final int MINERFACTORY_LOW_SUPPLY = initialSupply(MINER);
    public static final int MINERFACTORY_RESUPPLY_AMT = 5 * initialSupply(MINER);

    public static final int MINER_LOW_SUPPLY = 75;
    // Get this much from the HQ.
    public static final int MINER_RESUPPLY_FROM_HQ = 2000;

    // Call for resupply at this point.
    public static final int TANKFACTORY_LOW_SUPPLY = initialSupply(TANK);
    public static final int TANKFACTORY_RESUPPLY_AMT = 3 * initialSupply(TANK);

    // Number of early harassment drones.
    public static final int DRONE_HARRASS_N = 5;

    // Maximum times we will build a commander
    public static final int MAX_COMMANDER_SPAWN_COUNT = 3;

    public static final int RALLY_ARMY = 0;

    // How close must be to target point. (sqrads)
    public static final int MOVEMENT_CLUMP_DEFAULT = 25;

    // How close to encircle a target point. (sqrads)
    public static final int MOVEMENT_CIRCLE_DEFAULT = 4*4;

    public static final int MAXIMUM_ATTACK_RANGE_EVER = 6*6;

    // There are X types of robots.
    // Hopefully rtype.ordinal() has a max value of X-1
    public static final int NUM_ROBOT_TYPES = 21;
    // public static final int NUM_ROBOT_TYPES = RobotType.values().length;

    public static int taskSupply(int task) {
        switch(task) {
            case Task.NONE:                 return 0;
            case Task.MINE:                 return initialSupply(BEAVER);
            case Task.BARRACKS:             return initialSupply(BEAVER);
            case Task.MINERFACTORY:         return initialSupply(BEAVER) + initialSupply(MINERFACTORY);
            case Task.TANKFACTORY:          return initialSupply(BEAVER) + initialSupply(TANKFACTORY);
            case Task.HELIPAD:              return initialSupply(BEAVER) + initialSupply(HELIPAD);
            case Task.TECHNOLOGYINSTITUTE:  return initialSupply(BEAVER);
            case Task.TRAININGFIELD:        return initialSupply(BEAVER) + initialSupply(TRAININGFIELD);
            case Task.SUPPLYDEPOT:          return initialSupply(BEAVER) + initialSupply(SUPPLYDEPOT);
            case Task.AEROSPACELAB:         return initialSupply(BEAVER) + initialSupply(AEROSPACELAB);
            case Task.RESUPPLY_TANKFACTORY: return initialSupply(BEAVER) + TANKFACTORY_RESUPPLY_AMT;
            default:                        throw new NotImplementedException();
        }
    }

    public static int initialSupply(RobotType rtype) {
        switch (rtype) {
            // TODO(miles): decrease this by a metrick fuckton.
            case BEAVER:              return 1000;

            case MINERFACTORY:        return 10 * initialSupply(MINER);
            case MINER:               return 1000;

            case BARRACKS:            return 10 * initialSupply(SOLDIER);
            case SOLDIER:             return 1000;
            case BASHER:              return 1000;

            case HELIPAD:             return DRONE_HARRASS_N * initialSupply(DRONE);
            case DRONE:               return 3000;

            case TECHNOLOGYINSTITUTE: return 0;

            case TRAININGFIELD:       return initialSupply(COMMANDER);
            case COMMANDER:           return 3000;

            case TANKFACTORY:         return 6 * initialSupply(TANK);
            case TANK:                return 4000;

            case AEROSPACELAB:        return 5 * initialSupply(LAUNCHER);
            case LAUNCHER:            return 1000;
            case MISSILE:             return 0;

            case SUPPLYDEPOT:         return 0;

            case COMPUTER:            throw new NotImplementedException();
            case HANDWASHSTATION:     throw new NotImplementedException();
            case HQ:                  throw new NotImplementedException();
            case TOWER:               throw new NotImplementedException();
            default:                  throw new NotImplementedException();
        }
    }

    public static final int EARLY_RALLY_GROUP_1 = 500;
    public static final int ATTACK_GROUP_1 = 1000;
    public static final int ATTACK_GROUP_2 = 1700;

    public static final int armyGroupFromRound(int round) {
        if (round <= ATTACK_GROUP_1 - 10) return 0;
        else                              return 1;
    }
}
