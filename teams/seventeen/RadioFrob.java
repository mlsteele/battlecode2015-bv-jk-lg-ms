package seventeen;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

// Radio module to abstract away our radio protocol.
// Caches slots of data.
// Use 'load' methods to read from the broadcast stream (expensive).
// Use 'get' methods to get cached data (will not cause a load, so be careful).
// Use 'write' methods to change and broadcast.
public class RadioFrob {
    private static int INTENTION_SLOT = 0;

    private RobotController rc;
    private int[] radioCache = new int[GameConstants.BROADCAST_MAX_CHANNELS];

    RadioFrob(RobotController rc) {
        this.rc = rc;
    }

    // public loadIntentions() {
    // }
}
