package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.lang.RuntimeException;

public class XUnits extends RadioModule {
    XUnits(RobotController rc, int lowestSlot) {
        super(rc, lowestSlot);
    }

    @Override
    public int slotsRequired() {
        // Add 6 just in case ;)
        return NUM_ROBOT_TYPES + 6;
    }

    // Requests there be x of a type of unit on the field.
    // Used by HQ to regulate economy.
    public void set(RobotType rtype, int x) {
        tx(lowestSlot + rtype.ordinal(), x);
    }

    // Check how many units of a type are desired.
    // Used by unit production facilities
    public int get(RobotType rtype) {
        return rx(lowestSlot + rtype.ordinal());
    }
}
