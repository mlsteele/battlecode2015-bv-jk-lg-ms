package team017.radio;

import battlecode.common.RobotController;

public class FloatSlot extends RadioModule {
    private int nSamples;
    private int ringIndex;
    private float[] samples;
    private float avg;

    // `nSamples` is how many samples to average over.
    FloatSlot(RobotController rc, int lowestSlot, int nSamples) {
        super(rc, lowestSlot);
        this.nSamples = nSamples;
        this.samples = new float[nSamples];
    }

    @Override
    public int slotsRequired() {
        return 1;
    }

    public void set(float x) {
        tx(lowestSlot, Float.floatToRawIntBits(x));
    }

    public float get() {
        return Float.intBitsToFloat(rx(lowestSlot));
    }

    // Side-effect triggers a rotation of the sample ring.
    // Don't get confused.
    public float getAveraged() {
        final float v = get();
        samples[ringIndex] = v;
        avg += v / nSamples;
        ringIndex = (ringIndex + 1) % nSamples;
        avg -= samples[ringIndex] / nSamples;
        return avg;
    }
}
