package droneHarrass.structures;

import static battlecode.common.RobotType.*;
import static droneHarrass.Strategy.*;
import droneHarrass.*;
import battlecode.common.*;

public class TechnologyInstitute extends Structure {
    public TechnologyInstitute(RobotController rc) { super(rc); }

    @Override
    public void run() {

        // Main loop
        while (true) {

            // We arent using computers so dont do anything
            rc.yield();
        }
    }

}

