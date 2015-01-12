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
    protected void resupplyNearby(RobotInfo[] candidates, RobotType rtype, int lowSupplyThreshold, int supply) {
        if (candidates == null) {
            candidates = rc.senseNearbyRobots(
                GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                rc.getTeam());
        }

        int minerID = 0;
        int supplyGoal = supply;
        int[] minerResupplyRequest = null;

        // special situation for miners
        if (rtype == MINER) {
            // Check to see if a miner is requesting supply
            minerResupplyRequest = rf.checkMinerResupply();
            if (minerResupplyRequest[0] == 0) return; // no one requested a resupply

            minerID    = minerResupplyRequest[0];
            supplyGoal = minerResupplyRequest[1];

            supplyToID(candidates, minerID, supplyGoal);
            rf.clearMinerResupply();
            return;

        } else {
            supplyGoal = supply;
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
        if (rc.getSupplyLevel() < supplyAmount)
            return false;

        if (candidates == null) {
            candidates = rc.senseNearbyRobots(
                    GameConstants.SUPPLY_TRANSFER_RADIUS_SQUARED,
                    rc.getTeam());
        }

        for (RobotInfo r : candidates) {
            // Only send to robot with the correct ID
            if (r.ID != robotID) continue;

            try {
                rc.transferSupplies(supplyAmount, r.location);
                return true;
            } catch (GameActionException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    // returns the current robot counts (includes units and structures)
    protected int[] getUnitCount() {
        int[] unitsOnField = new int[NUM_ROBOT_TYPES];
        RobotInfo[] robots = rc.senseNearbyRobots(Integer.MAX_VALUE, rc.getTeam());
        for (RobotInfo rob : robots) {
            unitsOnField[rob.type.ordinal()]++;
        }
        return unitsOnField;
    }

}
