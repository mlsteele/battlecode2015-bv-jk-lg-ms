package team017.units;

import static battlecode.common.Direction.*;
import team017.*;
import battlecode.common.*;

public class Miner extends Unit {
    public Miner(RobotController rc) { super(rc); }

    private MapLocation lastSeen = rc.getLocation();
    // TODO(miles): this should be 10, but I'm having lots of issues :(
    private final double ORE_CUTOFF = 12;
    private double awesomeOreAmount = 0; // best seen so far

    private final int ITS_BEEN_A_WHILE = 100; // num turns till give up on dreams
    private int roundLastMined = 0;
    private double bestOreInAWhile = 0;
    private int supplyRequest = 0;
    private boolean resupplying = false;
    private int resupplyStartRound = 0;

    @Override
    public void run() {
        while (true) {
            if (Analyze.ON) Analyze.aggregate("miners_supply", rc.getSupplyLevel());

            if (Math.abs(Clock.getRoundNum() - Strategy.KAMIKAZE) <= 1) kamikaze();
            
            MapLocation targetLoc = rf.orelocations.getLocation();
            if (targetLoc != null && rc.getLocation().distanceSquaredTo(targetLoc) <= RobotType.MINER.sensorRadiusSquared) {
                rf.orelocations.updateLocation((int) rc.senseOre(targetLoc));
            }

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
                    resupplyStartRound = Clock.getRoundNum();
                    rf.minerresupply.request(supplyRequest);
                } else {
                    if (resupplying) {
                        Bugging.setParams(lastSeen, 0, true);
                        Bugging.move();
                        rc.setIndicatorString(1, "Heading back to last seen best ore");

                        // if we are where we came from, or we found some awesome ore, stop going back
                        Direction nearbyGoodOre = optimalOreDirection();
                        if(rc.getLocation().equals(lastSeen) ||
                                (nearbyGoodOre != null && rc.senseOre(rc.getLocation().add(nearbyGoodOre)) >= awesomeOreAmount)) {
                            resupplying = false;
                            // Fudge last mining time so resupplying won't make us forget our dreams
                            int resupplyTime = Clock.getRoundNum() - resupplyStartRound;
                            roundLastMined += resupplyTime;
                        }
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

        // Move away from bad things.
        // TODO you could move smarter than this
        MapLocation enemyLoc = isLocationSafe(rc.getLocation());
        if (enemyLoc != null)
            moveTowardBugging(enemyLoc.directionTo(rc.getLocation()), true);

    }

    private void pursueMining() {
        double oreHere = rc.senseOre(rc.getLocation());
        if (oreHere > awesomeOreAmount) awesomeOreAmount = oreHere;
        if (oreHere > bestOreInAWhile) bestOreInAWhile = oreHere;
        int roundsSinceLastMine = Clock.getRoundNum() - roundLastMined;
        if (oreHere >= ORE_CUTOFF || (oreHere > 0 && (roundsSinceLastMine >= ITS_BEEN_A_WHILE || oreHere >= awesomeOreAmount))) {
            rc.setIndicatorString(1, "mining here");
            bestOreInAWhile = oreHere;
            mineHere();
        } else {
            Direction oreTarget = optimalOreDirection();
            if (oreTarget != null) {
                try {
                    rc.setIndicatorString(1, "mining but moving to ore target");
                    forward = oreTarget;
                    if (isLocationSafe(rc.getLocation().add(forward))!=null) System.out.println("Miner lives the dangerous lifestyle");
                    rc.move(forward);
                } catch (GameActionException e) {
                    e.printStackTrace();
                }
            } else {
                if (rf.orelocations.getAmount() > ORE_CUTOFF 
                        && rc.getLocation().distanceSquaredTo(rf.orelocations.getLocation()) < 12*12) 
                        moveTowardBugging(rf.orelocations.getLocation(), true);
                else {
                    rc.setIndicatorString(1, "mining but wandering");
                    wander(true);
                }
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
            roundLastMined = Clock.getRoundNum();
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
            if ((ore > bestOre) // better than other stuff around
                    && (ore > ORE_CUTOFF || ore >= awesomeOreAmount) // good enough to be worth it
                    && rc.canMove(d) // valid to move to
                    && isLocationSafe(curLocation.add(d)) == null) { // won't get shot probably
                bestOre = ore;
                bestDirection = d;
            }
        }

        if (bestDirection != null) reportOreHere(rc.getLocation().add(bestDirection), (int) bestOre);

        return bestDirection;
    }
    
    private void kamikaze() {
        while (true) {
            shootBaddies();
            Bugging.setParams(rc.senseEnemyHQLocation(), 0, false);
            if (rc.isCoreReady()) Bugging.move();
            rc.setIndicatorString(1, "ATTACKKK");
        }
    }
}
