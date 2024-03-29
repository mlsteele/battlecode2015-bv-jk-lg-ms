package team017;

import battlecode.common.*;

/**
 * Tasks are given by Headquarters to Beavers, who use them to perform tasks.
 * taskNums are supplied by the Task. constants in Strategy.
 */
public class Task {
    // Task codes that HQ gives to beavers.
    public static final int REQUESTING_TASK = -1;
    public static final int NONE = 0;
    public static final int MINE = 1;
    public static final int MINERFACTORY = 2;
    public static final int SUPPLYDEPOT = 3;
    public static final int BARRACKS = 4;
    public static final int TANKFACTORY = 5;
    public static final int HELIPAD = 6;
    public static final int AEROSPACELAB = 7;
    public static final int TECHNOLOGYINSTITUTE = 8;
    public static final int TRAININGFIELD = 9;
    public static final int HANDWASHSTATION = 10;
    public static final int RESUPPLY_STRUCTURE = 11;

    public final int taskNum;
    public final MapLocation loc;
    public final int amount;

    // `taskNum` must [0, 15] (4 bits)
    // `amount` must be in [0, 409500] (12 bits)
    // `amount` will be rounded to a multiple of 100
    public Task(int taskNum, MapLocation loc, int amount) {
        if (taskNum < 0 || taskNum > 15)
            System.err.println("ERROR: taskNum out of bounds");
        amount = (amount / 100) * 100;
        if (amount < 0 || amount > 409500)
            System.err.println("ERROR: amount out of bounds");
        this.taskNum = taskNum;
        this.loc = loc;
        this.amount = amount;

        if (taskNum == RESUPPLY_STRUCTURE && loc == null)
            System.err.println("ERROR: resupply structure mission instantiated with null location");
    }

    public Task(int taskNum) {
        this(taskNum, null, 0);
    }

    public Task(int taskNum, MapLocation loc) {
        this(taskNum, loc, 0);
    }

    public RobotType structureType() {
        switch (taskNum) {
            case MINERFACTORY:        return RobotType.MINERFACTORY;
            case SUPPLYDEPOT:         return RobotType.SUPPLYDEPOT;
            case BARRACKS:            return RobotType.BARRACKS;
            case TANKFACTORY:         return RobotType.TANKFACTORY;
            case HELIPAD:             return RobotType.HELIPAD;
            case AEROSPACELAB:        return RobotType.AEROSPACELAB;
            case TECHNOLOGYINSTITUTE: return RobotType.TECHNOLOGYINSTITUTE;
            case TRAININGFIELD:       return RobotType.TRAININGFIELD;
            case HANDWASHSTATION:     return RobotType.HANDWASHSTATION;
            default:
                System.err.println("ERROR: tried to get buildingType for bad task number " + taskNum);
                return RobotType.SUPPLYDEPOT;
        }
    }

    public int requiredOre() {
        switch (taskNum) {
            case MINERFACTORY:        return RobotType.MINERFACTORY.oreCost;
            case SUPPLYDEPOT:         return RobotType.SUPPLYDEPOT.oreCost;
            case BARRACKS:            return RobotType.BARRACKS.oreCost;
            case TANKFACTORY:         return RobotType.TANKFACTORY.oreCost;
            case HELIPAD:             return RobotType.HELIPAD.oreCost;
            case AEROSPACELAB:        return RobotType.AEROSPACELAB.oreCost;
            case TECHNOLOGYINSTITUTE: return RobotType.TECHNOLOGYINSTITUTE.oreCost;
            case TRAININGFIELD:       return RobotType.TRAININGFIELD.oreCost;
            case HANDWASHSTATION:     return RobotType.HANDWASHSTATION.oreCost;
            default:                  return 0;
        }
    }

    @Override
    public String toString() {
        return "Task(" + taskNum + ", " + loc + ", " + amount + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Task)) return false;
        if (other == this) return true;
        Task that = (Task) other;

        return
        taskNum == that.taskNum &&
        (loc == null ? (that.loc == null) : loc.equals(that.loc)) &&
        amount == that.amount;
    }

    @Override
    public int hashCode() {
        return taskNum + (loc == null? 0 : loc.hashCode()) + amount;
    }
}
