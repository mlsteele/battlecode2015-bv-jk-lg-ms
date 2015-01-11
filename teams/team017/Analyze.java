package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

// Format match output for analysis.
// TODO(miles): Should we use matching logging or something for this?
public enum Analyze {;
    public static void sampleTeamOre(RobotController rc) {
        System.out.println("ANALYZE team ore " + rc.getTeamOre());
    }

    public static void count(String label, int n) {
        System.out.println("ANALYZE count " + label + " " + n);
    }
}
