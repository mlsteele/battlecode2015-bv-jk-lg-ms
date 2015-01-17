package team017;

import battlecode.common.*;
import static battlecode.common.Direction.*;
import static battlecode.common.RobotType.*;
import static team017.Strategy.*;

public class BeaverTasks extends RadioModule {
    private int BEAVER_TASK_ASSIGNMENT_SLOT;
    private int BEAVER_TASK_BASE;
    private int freeBeaverTaskSlot = 0;

    BeaverTasks(RobotController rc, int lowestSlot) {
        super(rc, lowestSlot);
        BEAVER_TASK_ASSIGNMENT_SLOT = lowestSlot;
        BEAVER_TASK_BASE            = lowestSlot + 1;
    }

    @Override
    public int slotsRequired() {
        // TODO(miles): I guess we probably won't have >1000
        //              beavers but this is mad sketch.
        return 1000;
    }

    // Assigns a task to the beaver slot. Returns task assignment slot
    // returns -1 if slot has not been claimed
    // HQ ONLY
    public int assignBeaverTaskSlot() {
        if (rx(BEAVER_TASK_ASSIGNMENT_SLOT) != 0) {
            return -1;
        } else {
            freeBeaverTaskSlot++;
            tx(BEAVER_TASK_ASSIGNMENT_SLOT, freeBeaverTaskSlot);
            return freeBeaverTaskSlot;
        }
    }

    // gets the taskSlot of the beaver who was assigned one (used by beaver)
    // clears it to let the hq know it got its taskSlot
    // BEAVER ONLY
    public int getBeaverTaskSlot() {
        int taskSlot = rx(BEAVER_TASK_ASSIGNMENT_SLOT);
        tx(BEAVER_TASK_ASSIGNMENT_SLOT, 0);
        return taskSlot;
    }

    // Returns the task at a given taskSlot
    // Returns null if it is in the requesting task state.
    // BEAVER ONLY
    public Task getTask(int taskSlot) {
        int taskSerial = rx(BEAVER_TASK_BASE + taskSlot);
        if (taskSerial == Task.REQUESTING_TASK) {
            return null;
        } else {
            return decodeTask(taskSerial);
        }
    }

    // sets a task for the given beaver taskSlot
    // HQ ONLY
    public boolean setTask(Task task, int taskSlot) {
        tx(BEAVER_TASK_BASE + taskSlot, encodeTask(task));
        return true;
    }

    // used by beaver to request a task
    // BEAVER ONLY
    public void requestTask(int myTaskSlot) {
        tx(BEAVER_TASK_BASE, myTaskSlot);
        tx(BEAVER_TASK_BASE + myTaskSlot, Task.REQUESTING_TASK);
    }

    // used by hq, returns the taskslot of the beaver assigned the task
    // returns -1 if it cant verify the beaver wants a job
    // HQ ONLY
    public int assignTaskToNextFree(Task task) {
        int taskSlot = rx(BEAVER_TASK_BASE);
        if (taskSlot > 0 && (rx(BEAVER_TASK_BASE + taskSlot) == Task.REQUESTING_TASK)) {
            tx(BEAVER_TASK_BASE, 0);
            setTask(task, taskSlot);
            return taskSlot;
        } else return -1;
    }

}
