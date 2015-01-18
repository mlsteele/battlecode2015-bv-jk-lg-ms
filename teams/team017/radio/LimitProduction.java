package team017;

import battlecode.common.RobotController;
import battlecode.common.RobotType;

/**
 * LimitProduction disables the production of units.
 * XUnits has precidence over LimitProduction.
 *
 * In the radio, a value of 0 means that the unit can be built.
 * A value of 1 means the unit should not be built.
 * This is backwards from normal so that 0 is an acceptable initial value.
 */
public class LimitProduction extends RadioModule {

    LimitProduction(RobotController rc, int lowestSlot) {
        super(rc, lowestSlot);
    }

    @Override
    public int slotsRequired() {
        return Strategy.NUM_ROBOT_TYPES;
    }

    public void stopBuilding(RobotType r) {
        tx(lowestSlot + r.ordinal(), 1);
    }

    public void resumeBuilding(RobotType r) {
        tx(lowestSlot + r.ordinal(), 0);
    }

    public boolean shouldBuild(RobotType r) {
        return rx(lowestSlot + r.ordinal()) == 0;
    }

}
