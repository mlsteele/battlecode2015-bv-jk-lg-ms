package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;
import java.lang.RuntimeException;

// Radio module to abstract away our radio protocol.
public class RadioFrob {
    public RallyPoints rallypoints;
    public XUnits xunits;
    public Resupply resupply;
    public BeaverTasks beavertasks;
    public MinerResupply minerresupply;
    public FloatSlot miningrate;
    public LimitProduction limitproduction;

    private RobotController rc;

    RadioFrob(RobotController rc) {
        this.rc = rc;
        int nextSlot = 0;

        rallypoints = new RallyPoints(rc, nextSlot);
        nextSlot += rallypoints.slotsRequired();

        xunits = new XUnits(rc, nextSlot);
        nextSlot += xunits.slotsRequired();

        resupply = new Resupply(rc, nextSlot);
        nextSlot += resupply.slotsRequired();

        beavertasks = new BeaverTasks(rc, nextSlot);
        nextSlot += beavertasks.slotsRequired();

        minerresupply = new MinerResupply(rc, nextSlot);
        nextSlot += minerresupply.slotsRequired();

        miningrate = new FloatSlot(rc, nextSlot, 40);
        nextSlot += miningrate.slotsRequired();

        limitproduction = new LimitProduction(rc, nextSlot);
        nextSlot += limitproduction.slotsRequired();

        if (rc.getType() == HQ) {
            System.out.println("RadioFrob claimed " + nextSlot + " of " + GameConstants.BROADCAST_MAX_CHANNELS + " channels");
        }
    }
}