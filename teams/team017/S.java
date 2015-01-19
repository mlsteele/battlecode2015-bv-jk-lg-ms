package team017;

import java.util.*;
import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import team017.*;
import team017.radio.*;
import team017.structures.*;
import team017.units.*;
import static team017.Strategy.*;

// Stored static variables.
public enum S {;
    public static RobotController rc;
    public static final Direction[] directions =
        {NORTH, NORTH_EAST, EAST, SOUTH_EAST,
         SOUTH, SOUTH_WEST, WEST, NORTH_WEST};
}
