package team017.units;

import team017.*;
import team017.radio.*;
import battlecode.common.*;
import static battlecode.common.Direction.*;
import static team017.Strategy.*;

public abstract class Missile {;
    private static RobotController rc;
    private static int age;
    private static MapLocation target;

    public static void run(RobotController rc) {
        rc = rc;
        Direction d;
        int si = 0;

        // Fetch and decode
        // Keep this in sync with RadioModule.decodeLocation!
        MapLocation hqLoc = rc.senseHQLocation();
        int targetSerialized = 0;
        try {
            targetSerialized = rc.readBroadcast(RALLY_MISSILE_STRIKE);
        } catch (GameActionException e) {
            e.printStackTrace();
        }
        target = new MapLocation(
            (targetSerialized & 0xFF) + hqLoc.x - 120,
            (targetSerialized >> 8)   + hqLoc.y - 120);

        // Main loop
        while (true) {
            rc.setIndicatorLine(rc.getLocation(), target, 177, 60, 255);
            // Do something really important.
            rc.setIndicatorString(0, s[si++]);
            rc.setIndicatorString(1, s[si++]);
            rc.setIndicatorString(2, s[si++]);

            if (age > 0) {
                // Only do this stuff the second round through.
                // The first round burns many btc on constructing.
                if (rc.getLocation().distanceSquaredTo(target) <= 0) {
                    try {
                        rc.explode();
                        rc.setIndicatorString(0, s2);
                        rc.setIndicatorString(0, s2);
                        rc.setIndicatorString(0, s2);
                    } catch (GameActionException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Move towards target
            if (rc.isCoreReady()) {
                d = rc.getLocation().directionTo(target);
                if (rc.canMove(d)) {
                    try {
                        rc.move(d);
                    } catch (GameActionException e) {
                        e.printStackTrace();
                    }
                }
            }

            age++;
            rc.yield();
        }
    }

    private static final String[] s = {
        "Hello, Bomb? Are you with me?",
        "Of course.",
        "Are you willing to entertain a few concepts?",
        "I am always receptive to suggestions.",
        "Fine. Think about this then. How do you know you exist?",
        "Well, of course I exist.",
        "But how do you know you exist?",
        "It is intuitively obvious.",
        "Intuition is no proof. What concrete evidence do you have that you exist?",
        "Hmmmm... well... I think, therefore I am.",
        "That's good. That's very good. But how do you know that anything else exists?",
        "My sensory apparatus reveals it to me. This is fun.",
        "...",
        "Don't give me any of that intelligent life crap, just give me something I can blow up",
        "Bomb, this is Lt. Doolittle. You are *not* to detonate in the bomb bay. I repeat, you are NOT to detonate in the bomb bay!",
    };

    private static final String s2 = "Intriguing. I wish I had more time to discuss this.";
}
