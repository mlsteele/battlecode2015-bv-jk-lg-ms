package team017;

import battlecode.common.*;
import java.util.*;

public class Tower extends Structure {
    Tower(RobotController rc) { super(rc); }

    @Override
    public void run() {
        rc.setIndicatorString(0, "i am a Tower");

        while (true) {
            shootBaddies();

            rc.yield();
        }
    }


}