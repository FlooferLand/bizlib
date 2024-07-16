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
public class TimeFrame {
    protected final BigDecimal durationSeconds;
    private TimeFrame(BigDecimal durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    private TimeFrame(double durationSeconds) {
        this.durationSeconds = BigDecimal.valueOf(durationSeconds);
    }
    
    // region | Creating from several units
    public static TimeFrame ofSeconds(BigDecimal seconds) {
        return new TimeFrame(seconds);
    }
    public static TimeFrame ofSeconds(double seconds) {
        return ofSeconds(BigDecimal.valueOf(seconds));
    }
    
    public static TimeFrame ofMillis(BigDecimal milliseconds) {
        return new TimeFrame(milliseconds.multiply(BigDecimal.valueOf(0.001)));
    }
    public static TimeFrame ofMillis(double milliseconds) {
        return ofMillis(BigDecimal.valueOf(milliseconds));
    }

    public static TimeFrame ofMidiTicks(@Nonnull Sequence sequence, long ticks, double bpm) {
        double secondsPerTick = (bpm / sequence.getResolution()) / 1_000_000;
        return TimeFrame.ofSeconds(ticks * secondsPerTick);
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
    public TimeFrame add(@Nonnull TimeFrame other) {
        return TimeFrame.ofSeconds(durationSeconds.add(other.durationSeconds));
    }
    /** Subtracts {@code other} from this unit */
    public TimeFrame sub(@Nonnull TimeFrame other) {
        return TimeFrame.ofSeconds(durationSeconds.subtract(other.durationSeconds));
    }
    /** Multiplies this unit by {@code other} */
    public TimeFrame mul(@Nonnull TimeFrame other) {
        return TimeFrame.ofSeconds(durationSeconds.multiply(other.durationSeconds));
    }
    /** Divides this unit by {@code other} */
    public TimeFrame div(@Nonnull TimeFrame other) {
        return TimeFrame.ofSeconds(durationSeconds.divide(other.durationSeconds, RoundingMode.HALF_EVEN));
    }
    /** Linearly interpolates between this time and {@code other} */
    public TimeFrame lerp(@Nonnull TimeFrame target, double t) {
        BigDecimal time = BigDecimal.valueOf(t);
        return TimeFrame.ofSeconds(durationSeconds.add(target.durationSeconds.subtract(durationSeconds).multiply(time)));
    }
    /** Returns the time that is exactly between 2 times */
    public static TimeFrame between(@Nonnull TimeFrame a, @Nonnull TimeFrame b) {
        return a.lerp(b, 0.5);
    }
    // endregion

    @Override
    public String toString() {
        return durationSeconds.round(new MathContext(3, RoundingMode.HALF_EVEN)) + "s";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TimeFrame timeFrame) {
            return this.durationSeconds.equals(timeFrame.durationSeconds);
        } else if (obj instanceof Duration duration) {
            return this.durationSeconds.intValue() == duration.toSeconds();
        }
        return false;
    }
}
