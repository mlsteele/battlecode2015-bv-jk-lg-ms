package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import java.util.*;

// Subclass for units that move and shoot and stuff.
public abstract class Structure extends Robot {
    Structure(RobotController rc) { super(rc); }

    // Give supplies to nearby robots who have no supplies.
    // Attempt to transfer `supplyAmount` supplies to nearby robots of type `rtype` who have 0 supply.
    // If candidates is null, they will be fetched automatically.
    protected void supplyNearbyEmpty(RobotInfo[] candidates, RobotType rtype, int supplyAmount) {
        if (rc.getSupplyLevel() < supplyAmount)
            return;

        if (candidates == null) {
            candidates = rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                rc.getTeam());
        }

        for (RobotInfo r : candidates) {
            // Only send to the correct type of bot.
            if (r.type != rtype) continue;
            if (r.supplyLevel > 0) continue;

            try {
                rc.transferSupplies(supplyAmount, r.location);
            } catch (GameActionException e) {
                e.printStackTrace();
            }

            // Abort when supplier run out of supplies.
            if (rc.getSupplyLevel() < supplyAmount) {
                return;
            }
        }
    }

}
