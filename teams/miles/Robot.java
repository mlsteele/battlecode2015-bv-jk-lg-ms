package miles;

import battlecode.common.*;
import java.util.*;

public abstract class Robot {
    protected RobotController rc;

    Robot(RobotController rc) {
        this.rc = rc;
    }

    abstract public void run();
}
