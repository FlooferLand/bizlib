package flooferland.showbiz.lowlevel.types.math;

import flooferland.chirp.types.math.LengthUnit;

import java.time.Duration;

/**
 * The measurement for the speed of a tape (PER SECOND) <br/>
 * RAE seem to use 7.5 IPS <br/>
 * <a href="https://chatgpt.com/share/e812c190-6e61-4074-af48-a3e9c1d7c398">ChatGPT explanation of stuff</a>
 */
@SuppressWarnings("unused")
public class TapeSpeed {
    protected final LengthUnit speedPerSecond;
    private TapeSpeed(LengthUnit speed) {
        this.speedPerSecond = speed;
    }
    
    // region | Creating from several units
    /** Creates a new TapeSpeed object from a speed (measured in Inches-Per-Second) */
    public static TapeSpeed ofInchesPerSecond(double inches) {
        return new TapeSpeed(LengthUnit.ofInches(inches));
    }
    /** Creates a new TapeSpeed object from a speed (measured in Meters-Per-Second) */
    public static TapeSpeed ofMetersPerSecond(double meters) {
        return new TapeSpeed(LengthUnit.ofMeters(meters));
    }
    // endregion
    
    // region | Fetching data in several units
    public double asInchesPerSecond() {
        return speedPerSecond.asInches();
    }
    public double asMetersPerSecond() {
        return speedPerSecond.asMeters();
    }
    // endregion

    // region | Math
    /** Adds {@code other} to this unit */
    public TapeSpeed add(TapeSpeed other) {
        return new TapeSpeed(speedPerSecond.add(other.speedPerSecond));
    }
    /** Subtracts {@code other} from this unit */
    public TapeSpeed sub(TapeSpeed other) {
        return new TapeSpeed(speedPerSecond.sub(other.speedPerSecond));
    }
    /** Multiplies this unit by {@code other} */
    public TapeSpeed mul(TapeSpeed other) {
        return new TapeSpeed(speedPerSecond.mul(other.speedPerSecond));
    }
    /** Divides this unit by {@code other} */
    public TapeSpeed div(TapeSpeed other) {
        return new TapeSpeed(speedPerSecond.div(other.speedPerSecond));
    }

    /** Gets the tape length from a duration */
    public LengthUnit calculateLength(Duration duration) {
        long seconds = duration.getSeconds();
        double nanoseconds = duration.getNano() / 1_000_000_000.0;
        double totalSeconds = seconds + nanoseconds;
        return speedPerSecond.mulRaw(totalSeconds);
    }
    // endregion

    @Override
    public String toString() {
        return speedPerSecond + " ips";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TapeSpeed tapeSpeed) {
            return this.speedPerSecond == tapeSpeed.speedPerSecond;
        }
        return false;
    }
}
