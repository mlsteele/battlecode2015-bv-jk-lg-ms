package team017;

import battlecode.common.*;
import battlecode.common.MapLocation;

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
}
