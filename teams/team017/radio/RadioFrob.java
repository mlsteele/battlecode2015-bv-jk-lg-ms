package team017.radio;

import static battlecode.common.RobotType.*;
import battlecode.common.*;

// Radio module to abstract away our radio protocol.
public class RadioFrob {
    public RallyPoints rallypoints;
    public XUnits xunits;
    public Resupply resupply;
    public BeaverTasks beavertasks;
    public MinerResupply minerresupply;
    public FloatSlot miningrate;
    public LimitProduction limitproduction;
    public OreLocations orelocations;

    public RadioFrob(RobotController rc) {
        int nextSlot = 0;

        // NOTE: Rally points MUST be located at channel 0.
        //       Because missiles use a rally point but don't use the frob.
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

        orelocations = new OreLocations(rc, nextSlot);
        nextSlot += orelocations.slotsRequired();

        if (rc.getType() == HQ) {
            System.out.println("RadioFrob claimed " + nextSlot + " of " + GameConstants.BROADCAST_MAX_CHANNELS + " channels");
        }
    }
}
