package team017.units;

import static battlecode.common.Direction.*;
import team017.*;
import battlecode.common.*;

public class Miner extends Unit {
	public Miner(RobotController rc) { super(rc); }

	private MapLocation lastSeen = rc.getLocation();
	private final double ORE_CUTOFF = 12;
	private double awesomeOreAmount = 0; // best seen so far
	private int supplyRequest = 0;
	private boolean resupplying = false;
	private boolean kamikaze = false;

	@Override
	public void run() {
		waitForSupplies();

		while (true) {
			if (Analyze.ON) Analyze.aggregate("miners_supply", rc.getSupplyLevel());

			if (Math.abs(Clock.getRoundNum() - Strategy.ATTACK_GROUP_2) <= 1) kamikaze = true;

			while (kamikaze) {
				shootBaddies();
				if (rc.isCoreReady()) moveTowardBugging(rc.senseEnemyHQLocation());
				rc.setIndicatorString(1, "ATTACKKK");
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
					rf.minerresupply.request(supplyRequest);
				} else {
					if (resupplying) {
						moveTowardBugging(lastSeen);
						rc.setIndicatorString(1, "Heading back to last seen best ore");
						// if we are where we came from, or we found some awesome ore, stop going back
						if(rc.getLocation().equals(lastSeen) || rc.senseOre(rc.getLocation().add(optimalOreDirection())) >= awesomeOreAmount)
							resupplying = false;
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
		if (oreHere > awesomeOreAmount) awesomeOreAmount = oreHere;
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

		if (bestOre > awesomeOreAmount) awesomeOreAmount = bestOre;
		return bestDirection;
	}
}
