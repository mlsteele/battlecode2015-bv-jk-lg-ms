package team017.radio;

import battlecode.common.RobotController;

public class MinerResupply extends RadioModule {
    private int MINER_REQUEST_RESUPPLY_ID_SLOT;
    private int MINER_REQUEST_RESUPPLY_AMOUNT_SLOT;

    MinerResupply(RobotController rc, int lowestSlot) {
        super(rc, lowestSlot);
        MINER_REQUEST_RESUPPLY_ID_SLOT = lowestSlot;
        MINER_REQUEST_RESUPPLY_AMOUNT_SLOT = lowestSlot + 1;
    }

    @Override
    public int slotsRequired() {
        return 2;
    }

    // used by the miner to set a resupply request
    public void request(int amount) {
        tx(MINER_REQUEST_RESUPPLY_ID_SLOT, rc.getID());
        tx(MINER_REQUEST_RESUPPLY_AMOUNT_SLOT, amount);
    }

    // used by the hq to check if a miner wants a resupply
    // returns a list with the first element being the miner id and the
    // second being the supply amount
    public int[] checkMinerResupply() {
        int minerID       = rx(MINER_REQUEST_RESUPPLY_ID_SLOT);
        int requestAmount = rx(MINER_REQUEST_RESUPPLY_AMOUNT_SLOT);
        int[] r = {minerID, requestAmount};
        return r;
    }

    // used to clear the miner supply request
    public void clearMinerResupply() {
        tx(MINER_REQUEST_RESUPPLY_ID_SLOT, 0);
        tx(MINER_REQUEST_RESUPPLY_AMOUNT_SLOT, 0);
    }
}
