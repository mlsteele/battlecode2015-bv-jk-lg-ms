package team017;

import static battlecode.common.RobotType.*;
import battlecode.common.*;

// Subclass for units that move and shoot and stuff.
public abstract class Structure extends Robot {
    // Whether a resupply has requested and not fulfilled.
    private boolean resupplyRequested = false;
    // When a resupply was requested.
    private int resupplyRequestedAtRound = 0;
    // Reissue requests after this timeout.
    private static final int RESUPPLY_REQUEST_TIMEOUT = 100;

    public Structure(RobotController rc) { super(rc); }

    // Attacking for structures.
    // Does not block.
    protected void shootBaddies() {
        int range = Strategy.attackRadiusSquared(true, rc.getType());
        Team enemy = rc.getTeam().opponent();

        RobotInfo[] enemies = rc.senseNearbyRobots(range, enemy);

        // Return if all clear.
        if (enemies.length == 0)
            return;

        if (rc.isWeaponReady()) {
            try {
                // Note: canAttackLocation seems useless (see engine source)
                if (rc.canAttackLocation(chooseTarget(enemies).location))
                    rc.attackLocation(chooseTarget(enemies).location);
                else
                    System.out.println("WARNING: Chose target out of range in shootBaddies\n"
                            + S.rc.senseTowerLocations().length + " towers, calculated attack radius is " 
                            + Strategy.attackRadiusSquared(true, rc.getType()));
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        }
    }


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
            minerResupplyRequest = rf.minerresupply.checkMinerResupply();
            if (minerResupplyRequest[0] == 0) return; // no one requested a resupply

            minerID    = minerResupplyRequest[0];
            supplyGoal = minerResupplyRequest[1];

            supplyToID(candidates, minerID, supplyGoal);
            rf.minerresupply.clearMinerResupply();
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
    protected int[] getUnitCounts() {
        int[] unitsOnField = new int[Strategy.NUM_ROBOT_TYPES];
        RobotInfo[] robots = rc.senseNearbyRobots(Integer.MAX_VALUE, rc.getTeam());
        for (RobotInfo rob : robots) {
            unitsOnField[rob.type.ordinal()]++;
        }
        return unitsOnField;
    }

    // Requests resupply if current supply is below `lowSupplyThreshold`.
    // Requests enough to fill up to `desiredSupply`.
    // Makes sure not to have multiple outstanding requests.
    protected void requestResupplyIfLow(int lowSupplyThreshold, int desiredSupply) {
        final double supply = rc.getSupplyLevel();
        if (supply <= lowSupplyThreshold) {
            // Low on supply, consider requesting.
            boolean timedOut = (Clock.getRoundNum() - resupplyRequestedAtRound) > RESUPPLY_REQUEST_TIMEOUT;
            if (!resupplyRequested || timedOut) {
                if (rf.resupply.request((int)(desiredSupply - supply))) {
                    rc.setIndicatorString(2, "sent request. repeat:" + resupplyRequested);
                    resupplyRequested = true;
                    resupplyRequestedAtRound = Clock.getRoundNum();
                }
            } else {
                rc.setIndicatorString(2, "waiting patiently for resupply");
                // Keep waiting for resupply to arrive.
            }
        } else {
            // All set, forget previous requests.
            rc.setIndicatorString(2, "no resupply required");
            resupplyRequested = false;
        }
    }

    // Whether it would be wise to spawn a robot right now.
    // Considers:
    // - core status
    // - supply level
    // - rf.limitproduction
    // - reserved ore
    // Does NOT Consider:
    // - rf.xunits
    protected boolean shouldSpawn(RobotType rtype) {
        return
            rc.isCoreReady() &&
            rc.getSupplyLevel() >= Strategy.initialSupply(rtype) &&
            rf.limitproduction.shouldBuild(rtype) &&
            rc.getTeamOre() > rf.beavertasks.getReservedOre();
    }

}
