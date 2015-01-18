package team017.radio;

import java.util.Hashtable;

import team017.Task;
import battlecode.common.RobotController;

// Task assignment system for Beavers.
// This module is used by the HQ and Beavers.
// Methods are marked with which unit should use them.
public class BeaverTasks extends RadioModule {
    // Each beaver gets a taskSlot, which is an index
    // into the task array starting at TASK_ARRAY_BASE.
    // taskSlot is the only usage of the word "slot"
    // that does not refer to a radio slot.
    //
    // Pairing Protocol:
    // HQ spawns a beaver
    // new beaver calls acquireTaskSlot()
    // HQ calls discoverBeaverTaskSlot() every turn
    //
    // Task Assignment Protocol:
    // beavers calls requestNewTask() until they get a task
    // hq calls assignTaskToNextFree()
    // beaver calls getTask()

    // Used to assign slots to beavers.
    private int NEXT_FREE_TASKSLOT_SLOT;
    // Used to report a beaver ID back to HQ.
    private int REPORT_ID_SLOT;
    // Stores which taskSlot a beaver is waiting for a task on.
    // A value of -1 means no one is waiting.
    // The intial value is 0, but that's ok because assignTaskToNextFree checks the taskSlot as well.
    private int AWAITING_TASK_SLOT;
    // Array for assigning tasks to beavers.
    private int TASK_ARRAY_BASE;

    BeaverTasks(RobotController rc, int lowestSlot) {
        super(rc, lowestSlot);
        NEXT_FREE_TASKSLOT_SLOT = lowestSlot;
        REPORT_ID_SLOT          = lowestSlot + 1;
        AWAITING_TASK_SLOT      = lowestSlot + 2;
        TASK_ARRAY_BASE         = lowestSlot + 3;
    }

    @Override
    public int slotsRequired() {
        // TODO(miles): I guess we probably won't have >1000
        //              beavers but this is mad sketch.
        return 3 + 1000;
    }

    // Discover a new beaver.
    // Discovered beavers have already found itself a task slot.
    // Adds a mapping to `beaverMap` if a beaver is discovered.
    // Could be a no-op.
    // `beaverMap` is a mapping from taskSlot -> beaverID
    // HQ ONLY
    public void discoverBeaverTaskSlot(Hashtable<Integer, Integer> beaverMap) {
        final int beaverID = rx(REPORT_ID_SLOT);
        // If no beaver is reporting an ID, no undiscovered beavers exist.
        if (beaverID == 0) return;

        // Read the discovered beaver's task slot.
        // Subtract 1 because the beaver has advanced the counter since.
        final int taskSlot = rx(NEXT_FREE_TASKSLOT_SLOT) - 1;

        // Zero the ID report slot so that we don't rediscover this beaver.
        tx(REPORT_ID_SLOT, 0);

        // Add an entry to the map.
        System.out.println("found beaver slot:"+taskSlot+" id:"+beaverID);
        beaverMap.put(taskSlot, beaverID);
    }

    // Sets the task for the given taskSlot
    // HQ ONLY
    public boolean setTask(int taskSlot, Task task) {
        tx(TASK_ARRAY_BASE + taskSlot, encodeTask(task));
        return true;
    }

    // Assign a task to a waiting taskSlot.
    // Returns the taskslot of the beaver assigned the task.
    // Returns -1 if it the task was not assigned.
    // HQ ONLY
    public int assignTaskToNextFree(Task task) {
        int taskSlot = rx(AWAITING_TASK_SLOT);
        if (taskSlot > -1 && (rx(TASK_ARRAY_BASE + taskSlot) == Task.REQUESTING_TASK)) {
            // Clear awaiting task slot and assign task.
            tx(AWAITING_TASK_SLOT, -1);
            setTask(taskSlot, task);
            return taskSlot;
        } else {
            // No awaiting task slot.
            return -1;
        }
    }

    // Called by new beavers once when they wake up.
    // Returns the beaver's task slot.
    // BEAVER ONLY
    public int acquireTaskSlot() {
        final int taskSlot = rx(NEXT_FREE_TASKSLOT_SLOT);
        // Increment the free task slot value.
        tx(NEXT_FREE_TASKSLOT_SLOT, taskSlot + 1);
        // Report the beaver ID to HQ.
        tx(REPORT_ID_SLOT, rc.getID());
        return taskSlot;
    }

    // Get the task at a given taskSlot.
    // Returns NULL if it is in the requesting task state.
    // BEAVER ONLY
    public Task getTask(int taskSlot) {
        int taskSerialized = rx(TASK_ARRAY_BASE + taskSlot);
        if (taskSerialized == Task.REQUESTING_TASK) {
            return null;
        } else {
            return decodeTask(taskSerialized);
        }
    }

    // Request a new task from HQ.
    // Should be called repeatedly until a new task is acquired.
    // BEAVER ONLY
    public void requestNewTask(int myTaskSlot) {
        tx(TASK_ARRAY_BASE + myTaskSlot, Task.REQUESTING_TASK);
        tx(AWAITING_TASK_SLOT, myTaskSlot);
    }
}
