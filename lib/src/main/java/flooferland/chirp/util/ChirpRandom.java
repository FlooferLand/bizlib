package flooferland.chirp.util;

@SuppressWarnings("unused")
public final class ChirpRandom {
    // region | Range
    public static int range(int min, int max) {
        return (int) ((Math.random() * (max - min)) + min);
    }
    public static float range(float min, float max) {
        return (float) ((Math.random() * (max - min)) + min);
    }
    public static double range(double min, double max) {
        return (Math.random() * (max - min)) + min;
    }
    // endregion
}
