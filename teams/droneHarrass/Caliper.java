package droneHarrass;

import battlecode.common.Clock;

// Bytecode caliper for measuring how many bytecodes tasks take.
// Doesn't work so hot across turn boundaries.
//
// Example usage:
// Caliper c = new Caliper("frob");
// frob(); // operation to be timed.
// c.end(); // prints time t System.out
public class Caliper {
    private static final int subtractSelf = 47;

    private String label;
    private int start;

    public Caliper(String label) {
        this.label = label;
        start = Clock.getBytecodeNum();
    }

    public void end() {
        System.out.println(
            "Caliper " + label + " " +
            (Clock.getBytecodeNum() - start - subtractSelf));
    }
}
