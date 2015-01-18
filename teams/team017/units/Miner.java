package team017.units;

import static battlecode.common.Direction.EAST;
import static battlecode.common.Direction.NORTH;
import static battlecode.common.Direction.NORTH_EAST;
import static battlecode.common.Direction.NORTH_WEST;
import static battlecode.common.Direction.SOUTH;
import static battlecode.common.Direction.SOUTH_EAST;
import static battlecode.common.Direction.SOUTH_WEST;
import static battlecode.common.Direction.WEST;
import team017.Analyze;
import team017.Strategy;
import team017.Unit;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class Miner extends Unit {
    public Miner(RobotController rc) { super(rc); }

    private MapLocation lastSeen = rc.getLocation();
    private double ORE_CUTOFF = 12;
    private int supplyRequest = 0;
    private boolean resupplying = false;

    @Override
    public void run() {
        waitForSupplies();

        while (true) {
            if (Analyze.ON) Analyze.aggregate("miners_supply", rc.getSupplyLevel());

            // Top priority is don't get shot.
            if (rc.isCoreReady()) runAway();

            if (rc.isCoreReady()) {
                boolean miner_low_supply = rc.getSupplyLevel() <= Strategy.MINER_LOW_SUPPLY;
                boolean team_low_ore     = rc.getTeamOre()     <= Strategy.TEAM_LOW_ORE;
                if (miner_low_supply && team_low_ore) {
                    // we have run out of ore, lets save the last location we were at so we can return
                    lastSeen = rc.getLocation();
                    supplyRequest = 8 * lastSeen.distanceSquaredTo(hqLoc) + 4000;
                    goToHQ();
                    dumpSuppliesToHQ();
                    resupplying = true;
                    rf.minerresupply.request(supplyRequest);
                } else {
                    if (resupplying) {
                        if(rc.getLocation().equals(lastSeen)) resupplying = false;
                        moveTowardBugging(lastSeen);
                        rc.setIndicatorString(1, "Heading back to last seen best ore");
                    } else {
                        pursueMining();
                    }
                }
            }

            rc.yield();
        }
    }

    // If an enemy robot is nearby, go the other direction
    private void runAway() {
        int range = rc.getType().sensorRadiusSquared;
        Team enemy = rc.getTeam().opponent();

        RobotInfo[] enemies = rc.senseNearbyRobots(range, enemy);

        // Move away from first bad guy.
        // TODO you could move smarter than this
        if (enemies.length > 0)
            moveTowardBugging(enemies[0].location.directionTo(rc.getLocation()));

    }

    private void pursueMining() {
        double oreHere = rc.senseOre(rc.getLocation());
        if (oreHere >= 12) {
            rc.setIndicatorString(1, "mining here");
            mineHere();
        } else {
            Direction oreTarget = optimalOreDirection();
            if (oreTarget != null) {
                try {
                    rc.setIndicatorString(1, "mining but moving to ore target");
                    forward = oreTarget;
                    rc.move(forward);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            } else {
                rc.setIndicatorString(1, "mining but wandering");
                wander();
            }
        }
    }

    // Returns true if mining occurred.
    // Assumes there is ore
    private boolean mineHere() {
        try {
            // Copied from engine.
            rc.mine();
            double baseOre = rc.senseOre(rc.getLocation());
            double oreGain = Math.max(Math.min(baseOre / GameConstants.MINER_MINE_RATE, GameConstants.MINER_MINE_MAX), GameConstants.MINIMUM_MINE_AMOUNT);
            rf.miningrate.set(rf.miningrate.get() + (float)(oreGain));
            return true;
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Which direction's adjacent location has the most ore.
    // Returns NULL or a location that has been checked for canMove.
    // A return of NULL means that there is no nearby ore.
    public Direction optimalOreDirection() {
        Direction[] possibleDirs = {NORTH, NORTH_EAST, EAST, SOUTH_EAST,
                                    SOUTH, SOUTH_WEST, WEST, NORTH_WEST};

        MapLocation curLocation = rc.getLocation();
        double bestOre = 0.1;
        Direction bestDirection = null;

        Direction d;
        int ri = rand.nextInt(8);
        for (int i = 0; i < 8; i++) {
            d = possibleDirs[(i + ri) % 8];
            double ore = rc.senseOre(curLocation.add(d));
            if ((ore > bestOre) && (ore > ORE_CUTOFF) && rc.canMove(d)) {
                bestOre = ore;
                bestDirection = d;
            }
        }

        return bestDirection;
    }
}
