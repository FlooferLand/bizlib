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
public class TimeLength {
    protected final BigDecimal durationSeconds;
    private TimeLength(BigDecimal durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    private TimeLength(double durationSeconds) {
        this.durationSeconds = BigDecimal.valueOf(durationSeconds);
    }
    
    // region | Creating from several units
    public static TimeLength ofSeconds(BigDecimal seconds) {
        return new TimeLength(seconds);
    }
    public static TimeLength ofSeconds(double seconds) {
        return ofSeconds(BigDecimal.valueOf(seconds));
    }
    
    public static TimeLength ofMillis(BigDecimal milliseconds) {
        return new TimeLength(milliseconds.multiply(BigDecimal.valueOf(0.001)));
    }
    public static TimeLength ofMillis(double milliseconds) {
        return ofMillis(BigDecimal.valueOf(milliseconds));
    }

    public static TimeLength ofMidiTicks(@Nonnull Sequence sequence, long ticks, double bpm) {
        double secondsPerTick = (bpm / sequence.getResolution()) / 1_000_000;
        return TimeLength.ofSeconds(ticks * secondsPerTick);
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
        return durationSeconds.divide(BigDecimal.valueOf(ticksPerSecond), RoundingMode.HALF_EVEN).intValue();  // CHECKME: Math may be wack
    }
    // endregion

    // region | Math
    /** Adds {@code other} to this unit */
    public TimeLength add(@Nonnull TimeLength other) {
        return TimeLength.ofSeconds(durationSeconds.add(other.durationSeconds));
    }
    /** Subtracts {@code other} from this unit */
    public TimeLength sub(@Nonnull TimeLength other) {
        return TimeLength.ofSeconds(durationSeconds.subtract(other.durationSeconds));
    }
    /** Multiplies this unit by {@code other} */
    public TimeLength mul(@Nonnull TimeLength other) {
        return TimeLength.ofSeconds(durationSeconds.multiply(other.durationSeconds));
    }
    /** Divides this unit by {@code other} */
    public TimeLength div(@Nonnull TimeLength other) {
        return TimeLength.ofSeconds(durationSeconds.divide(other.durationSeconds, RoundingMode.HALF_EVEN));
    }
    /** Linearly interpolates between this time and {@code other} */
    public TimeLength lerp(@Nonnull TimeLength target, double t) {
        BigDecimal time = BigDecimal.valueOf(t);
        return TimeLength.ofSeconds(durationSeconds.add(target.durationSeconds.subtract(durationSeconds).multiply(time)));
    }
    /** Returns the time that is exactly between 2 times */
    public static TimeLength between(@Nonnull TimeLength a, @Nonnull TimeLength b) {
        return a.lerp(b, 0.5);
    }
    // endregion

    @Override
    public String toString() {
        return durationSeconds.round(new MathContext(3, RoundingMode.HALF_EVEN)) + "s";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof TimeLength timeLength) {
            return this.durationSeconds.equals(timeLength.durationSeconds);
        } else if (obj instanceof Duration duration) {
            return this.durationSeconds.intValue() == duration.toSeconds();
        }
        return false;
    }
}
