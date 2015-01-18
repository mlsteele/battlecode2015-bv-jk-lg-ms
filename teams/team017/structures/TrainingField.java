package team017.structures;

import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import team017.*;
import battlecode.common.*;

public class TrainingField extends Structure {
    public TrainingField(RobotController rc) { super(rc); }

    private int commanderSpawnCount = 0;

    @Override
    public void run() {

        // Main loop
        while (true) {

            // lets make sure we only spawn the commander at most MAX_COMMANDER_SPAWN_COUNT
            // times
            if (commanderSpawnCount < MAX_COMMANDER_SPAWN_COUNT) {
                if (rc.hasCommander()) continue; // we have a commander already
                else {
                    Direction dir = spawn(COMMANDER);
                    if (dir != null) commanderSpawnCount++;
                }
            }

            supplyNearbyEmpty(null, COMMANDER, Strategy.initialSupply(COMMANDER));

            rc.yield();
        }
    }

}

