package team017;

import battlecode.common.*;
import battlecode.common.MapLocation;

import java.lang.Override;

/**
 * Tasks are given by Headquarters to Beavers, who use them to perform tasks.
 * taskNums are supplied by the Task. constants in Strategy.
 */
public class Task {
    // Task codes that HQ gives to beavers.
    public static final int REQUESTING_TASK = -1;
    public static final int NONE = 0;
    public static final int MINE = 1;
    public static final int BARRACKS = 2;
    public static final int MINERFACTORY = 3;
    public static final int TANKFACTORY = 4;
    public static final int HELIPAD = 5;
    public static final int SUPPLYDEPOT = 6;
    public static final int RESUPPLY_TANKFACTORY = 7;

    public final int taskNum;
    public final MapLocation loc;

    Task(int taskNum) {
        this.taskNum = taskNum;
        this.loc = null;
    }

    Task(int taskNum, MapLocation loc) {
        this.taskNum = taskNum;
        this.loc = loc;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Task)) return false;
        if (other == this) return true;
        Task that = (Task) other;
        if (that.taskNum == this.taskNum) {
            if (loc == null) return (that.loc == null);
            else             return (that.loc == this.loc);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return taskNum + (loc == null? 0 : loc.hashCode());
    }
}
