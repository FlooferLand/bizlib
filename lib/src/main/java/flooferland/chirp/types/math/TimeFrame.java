package flooferland.chirp.types.math;

import java.time.Duration;

/**
 * A unit in time.
 * Like Java's "Duration", except this one isn't shit as it stores doubles/floats
 */
@SuppressWarnings("unused")
public class TimeFrame {
    protected final double durationSeconds;
    private TimeFrame(double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    
    // region | Creating from several units
    public static TimeFrame ofSeconds(double seconds) {
        return new TimeFrame(seconds);
    }
    public static TimeFrame ofMillis(double milliseconds) {
        return new TimeFrame(milliseconds * 0.001);
    }
    // endregion
    
    // region | Fetching data in several units
    public double asSeconds() {
        return durationSeconds;
    }
    public double asMillis() {
        return durationSeconds * 1000;
    }
    // endregion

    // region | Math
    /** Adds {@code other} to this unit */
    public TimeFrame add(TimeFrame other) {
        return TimeFrame.ofSeconds(durationSeconds + other.durationSeconds);
    }
    /** Subtracts {@code other} from this unit */
    public TimeFrame sub(TimeFrame other) {
        return TimeFrame.ofSeconds(durationSeconds - other.durationSeconds);
    }
    /** Multiplies this unit by {@code other} */
    public TimeFrame mul(TimeFrame other) {
        return TimeFrame.ofSeconds(durationSeconds * other.durationSeconds);
    }
    /** Divides this unit by {@code other} */
    public TimeFrame div(TimeFrame other) {
        return TimeFrame.ofSeconds(durationSeconds / other.durationSeconds);
    }
    // endregion

    @Override
    public String toString() {
        return durationSeconds + "s";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeFrame timeFrame) {
            return this.durationSeconds == timeFrame.durationSeconds;
        }
        return false;
    }
}
