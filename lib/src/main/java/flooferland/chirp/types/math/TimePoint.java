package flooferland.chirp.types.math;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sound.midi.Sequence;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;

/**
 * An accurate unit in time.
 * Like Java's "Duration", except this one isn't shit as it stores doubles/floats.
 */
@SuppressWarnings("unused")
public class TimePoint {
    protected final BigDecimal durationSeconds;
    private TimePoint(BigDecimal durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    private TimePoint(double durationSeconds) {
        this.durationSeconds = BigDecimal.valueOf(durationSeconds);
    }
    
    // region | Creating from several units
    public static TimePoint ofSeconds(BigDecimal seconds) {
        return new TimePoint(seconds);
    }
    public static TimePoint ofSeconds(double seconds) {
        return ofSeconds(BigDecimal.valueOf(seconds));
    }
    
    public static TimePoint ofMillis(BigDecimal milliseconds) {
        return new TimePoint(milliseconds.multiply(BigDecimal.valueOf(0.001)));
    }
    public static TimePoint ofMillis(double milliseconds) {
        return ofMillis(BigDecimal.valueOf(milliseconds));
    }

    public static TimePoint ofMidiTicks(@Nonnull Sequence sequence, long ticks, double bpm) {
        double secondsPerTick = (bpm / sequence.getResolution()) / 1_000_000;
        return TimePoint.ofSeconds(ticks * secondsPerTick);
    }
    // endregion
    
    // region | Fetching data in several units
    public double asSeconds() {
        return durationSeconds.doubleValue();
    }
    public double asMillis() {
        return durationSeconds.multiply(BigDecimal.valueOf(1000)).doubleValue();
    }
    public long asMidiTicks(@Nonnull Sequence sequence, double bpm) {
        double ticksPerSecond = (bpm * sequence.getResolution()) * 1_000_000;
        return durationSeconds.divide(BigDecimal.valueOf(ticksPerSecond), RoundingMode.HALF_EVEN).intValue();
    }
    // endregion

    // region | Math
    /** Adds {@code other} to this unit */
    public TimePoint add(@Nonnull TimePoint other) {
        return TimePoint.ofSeconds(durationSeconds.add(other.durationSeconds));
    }
    /** Subtracts {@code other} from this unit */
    public TimePoint sub(@Nonnull TimePoint other) {
        return TimePoint.ofSeconds(durationSeconds.subtract(other.durationSeconds));
    }
    /** Multiplies this unit by {@code other} */
    public TimePoint mul(@Nonnull TimePoint other) {
        return TimePoint.ofSeconds(durationSeconds.multiply(other.durationSeconds));
    }
    /** Divides this unit by {@code other} */
    public TimePoint div(@Nonnull TimePoint other) {
        return TimePoint.ofSeconds(durationSeconds.divide(other.durationSeconds, RoundingMode.HALF_EVEN));
    }
    /** Linearly interpolates between this time and {@code other} */
    public TimePoint lerp(@Nonnull TimePoint target, double t) {
        BigDecimal time = BigDecimal.valueOf(t);
        return TimePoint.ofSeconds(durationSeconds.add(target.durationSeconds.subtract(durationSeconds).multiply(time)));
    }
    /** Returns the time that is exactly between 2 times */
    public static TimePoint between(@Nonnull TimePoint a, @Nonnull TimePoint b) {
        return a.lerp(b, 0.5);
    }
    // endregion

    @Override
    public String toString() {
        return durationSeconds.round(new MathContext(3, RoundingMode.HALF_EVEN)) + "s";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TimePoint timePoint) {
            return this.durationSeconds.equals(timePoint.durationSeconds);
        } else if (obj instanceof Duration duration) {
            return this.durationSeconds.intValue() == duration.toSeconds();
        }
        return false;
    }
}
