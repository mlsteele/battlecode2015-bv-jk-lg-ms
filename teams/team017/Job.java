package team017;

import battlecode.common.*;
import battlecode.common.MapLocation;

/**
 * Jobs are given by Headquarters to Beavers, who use them to perform tasks.
 * jobNums are supplied by the TASK_ constants in Strategy.
 */
public class Job {

    public final int jobNum;
    public final MapLocation loc;

    Job(int jobNum) {
        this.jobNum = jobNum;
        this.loc = null;
    }

    Job(int jobNum, MapLocation loc) {
        this.jobNum = jobNum;
        this.loc = loc;
    }
}