package team017;


// Format match output for analysis.
// TODO(miles): Should we use matching logging or something for this?
public enum Analyze {;
    public static boolean ON = "ON".equals(System.getProperty("bc.testing.team017.analyze"));

    // Record a quantity's value on a turn.
    // This should only happen once per label per round.
    // This is good for stuff measureable by one robot, like team ore.
    public static void sample(String label, int n) {
        System.out.println("ANALYZE sample " + label + " " + n);
    }

    public static void sample(String label, double n) {
        System.out.println("ANALYZE sample " + label + " " + n);
    }

    // Sum multiple recordings of a value from different robots on a turn.
    // This may happen multiple times per turn.
    // But only once per turn per robot.
    // This is good for stuff requiring multiple robots to figure out, like how many miners are fleeing enemies.
    public static void aggregate(String label, int n) {
        System.out.println("ANALYZE aggregate " + label + " " + n);
    }

    public static void aggregate(String label, double n) {
        System.out.println("ANALYZE aggregate " + label + " " + n);
    }
}
