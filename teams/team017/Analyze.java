package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

// Format match output for analysis.
// TODO(miles): Should we use matching logging or something for this?
public enum Analyze {;
    // Record a quantity's value on a turn.
    // This should only happen once per label per round.
    public static void sample(String label, int n) {
        System.out.println("ANALYZE sample " + label + " " + n);
    }

    public static void sample(String label, double n) {
        System.out.println("ANALYZE sample " + label + " " + n);
    }
}
