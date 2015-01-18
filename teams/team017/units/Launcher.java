package team017.units;

import team017.*;
import battlecode.common.*;
import static battlecode.common.Direction.*;
import static team017.Strategy.*;

public class Launcher extends Unit {
    public Launcher(RobotController rc) { super(rc); }

    @Override
    public void run() {
        waitForSupplies();

        // Main loop
        while (true) {
            MapLocation missileTarget = findTarget();
            launchMissile(missileTarget);

            if (missileTarget == null && rc.isCoreReady()) {
                MapLocation target = rf.rallypoints.get(rallyGroup);
                if (target != null) {
                    moveToRallyPoint(target);
                }
            }

            rc.yield();
        }
    }

    private MapLocation findTarget() {
        RobotInfo[] enemies = rc.senseNearbyRobots(
            25,
            rc.getTeam().opponent());

        if (enemies.length == 0) return null;

        RobotInfo target = enemies[0];
        double    targetScore = -1;
        for (RobotInfo r : enemies) {
            double damage_per_whatever = r.type.attackPower / r.type.attackDelay;
            double score = damage_per_whatever; // 1060
            if (score > targetScore) {
                target = r;
                targetScore = score;
            }
        }
        return target.location;
    }

    private void launchMissile(MapLocation target) {
        if (target == null) return;
        if (rc.getMissileCount() == 0) return;
        Direction dir = rc.getLocation().directionTo(target);

        // TODO(miles): don't give up this easily.
        if (!rc.canLaunch(dir)) return;

        try {
            rc.launchMissile(dir);
            rf.rallypoints.set(RALLY_MISSILE_STRIKE, target);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
    }
}
