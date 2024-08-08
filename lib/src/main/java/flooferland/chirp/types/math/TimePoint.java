package flooferland.chirp.types.math;

import javax.annotation.Nonnull;
import javax.sound.midi.Sequence;
import java.time.LocalTime;

/** A type that acts as a timestamp of sorts */
public class TimePoint {
    private LocalTime time;
    private TimePoint(LocalTime time) {
        this.time = time;
    }

    // region | Constructors
    public static TimePoint of(int hour, int minute, int second, int nanoOfSecond) {
        return new TimePoint(LocalTime.of(hour, minute, second, nanoOfSecond));
    }
    public static TimePoint of(int hour, int minute, int second) {
        return TimePoint.of(hour, minute, second, 0);
    }
    public static TimePoint of(int hour, int minute) {
        return TimePoint.of(hour, minute, 0, 0);
    }
    // endregion

    public static TimePoint ofMidiTicks(@Nonnull Sequence sequence, long ticks, double bpm) {
        double secondsPerTick = (bpm / sequence.getResolution()) / 1_000_000;
        return new TimePoint(LocalTime.ofNanoOfDay(Math.round(ticks / secondsPerTick)));
    }
    
    public long asMidiTicks(@Nonnull Sequence sequence, double bpm) {
        double ticksPerSecond = (bpm * sequence.getResolution()) * 1_000_000;
        return Math.round(time.toNanoOfDay() * ticksPerSecond);
    }

    @Override
    public String toString() {
        return time.toString();
    }
}
