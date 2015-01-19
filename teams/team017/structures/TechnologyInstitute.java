package team017.structures;

import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import team017.*;
import battlecode.common.*;

public class TechnologyInstitute extends Structure {
    public TechnologyInstitute(RobotController rc) { super(rc); }

    @Override
    public void run() {

        // Main loop
        while (true) {
            callForHelp();

            // We arent using computers so dont do anything
            rc.yield();
        }
    }

}

