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

    // Top up nearby robots with supplies.
    // Attempt to fill robots of type `rtype` so they have `supplyGoal` supplies.
    // Will fill up only robots with <= lowSupplyThreshold supplies.
    // If candidates is null, they will be fetched automatically.
    protected void resupplyNearby(RobotInfo[] candidates, RobotType rtype, int lowSupplyThreshold, int supplyGoal) {
        if (candidates == null) {
            candidates = rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                rc.getTeam());
        }

        for (RobotInfo r : candidates) {
            // Only send to the correct type of bot.
            if (r.type != rtype) continue;
            if (r.supplyLevel > lowSupplyThreshold) continue;

            try {
                rc.transferSupplies(supplyGoal - (int)r.supplyLevel, r.location);
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }

    protected boolean supplyToID(RobotInfo[] candidates, int robotID, int supplyAmount) {
        System.out.println("Hey im in supplyToID looking for " + robotID);
        if (rc.getSupplyLevel() < supplyAmount)
            return false;

        if (candidates == null) {
            System.out.println("scanning");
            candidates = rc.senseNearbyRobots(
                    GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                    rc.getTeam());
        }
        System.out.println("I didnt run out!");

        for (RobotInfo r : candidates) {
            // Only send to robot with the correct ID
            if (r.ID != robotID) continue;

            try {
                System.out.println("Hey I found the robot with id " + robotID);
                rc.transferSupplies(supplyAmount, r.location);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

}
