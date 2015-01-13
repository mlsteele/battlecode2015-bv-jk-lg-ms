package team017;

import battlecode.common.*;
import battlecode.common.MapLocation;

import java.lang.Override;

/**
 * Tasks are given by Headquarters to Beavers, who use them to perform tasks.
 * taskNums are supplied by the TASK_ constants in Strategy.
 */
public class Task {
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
