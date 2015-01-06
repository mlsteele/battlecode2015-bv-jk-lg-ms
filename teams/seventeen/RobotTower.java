package seventeen;

import battlecode.common.*;
import java.util.*;

public class RobotTower extends Robot {
    RobotTower(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a RobotTower");

        while (true) {
            if (rc.isWeaponReady()) {
                int range = rc.getType().attackRadiusSquared;
                Team enemy = rc.getTeam().opponent();
                RobotInfo[] enemies = rc.senseNearbyRobots(range, enemy);
                // TODO(miles): What's the poitn of canAttackLocation?
                if (enemies.length > 0) {
                    try {
                        rc.attackLocation(chooseTarget(enemies).location);
                    } catch (GameActionException e) {
                        e.printStackTrace();
                    }
                }
            }

            rc.yield();
        }
    }

    // Chooses the robot
    private RobotInfo chooseTarget(RobotInfo[] enemies) {
        RobotInfo best = enemies[0];
        for (RobotInfo rob : enemies) {
            if (rob.health < best.health) {
                best = rob;
            }
        }
        return best;
    }
}
