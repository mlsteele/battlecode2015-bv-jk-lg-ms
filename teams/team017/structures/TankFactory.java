package team017.structures;

import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import team017.*;
import battlecode.common.*;

public class TankFactory extends Structure {
    public TankFactory(RobotController rc) { super(rc); }

    private int[] unitCounts;

    @Override
    public void run() {
        while (true) {
            callForHelp();

            requestResupplyIfLow(
                    Strategy.initialSupply(TANK),
                    2*Strategy.initialSupply(TANK));

            if (shouldSpawn(TANK))
                spawn(TANK);

            supplyNearbyEmpty(null, TANK, initialSupply(TANK));

            rc.yield();
        }
    }

}
