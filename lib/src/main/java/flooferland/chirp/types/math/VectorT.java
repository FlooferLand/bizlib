package flooferland.chirp.types.math;

// TODO: Should maybe store the end instead of the duration internally

import javax.annotation.Nonnull;

/**
 * Like a Vector2, but with time!
 * Holds a start time and a duration; essentially just stores a length in time.
 */
public final class VectorT {
    @Nonnull private TimePoint start;
    @Nonnull private TimePoint duration;
    
    private VectorT(@Nonnull TimePoint start, @Nonnull TimePoint duration) {
        this.start = start;
        this.duration = duration;
    }
    
    // region | Constructors
    public static VectorT ofStartEnd(@Nonnull TimePoint start, @Nonnull TimePoint end) {
        return new VectorT(start, end.sub(start));
    }
    public static VectorT ofDuration(@Nonnull TimePoint start, @Nonnull TimePoint duration) {
        return new VectorT(start, duration);
    }
    // endregion

    // region | Getters
    public TimePoint getStart() {
        return start;
    }
    public TimePoint getDuration() {
        return start;
    }
    public TimePoint getEnd() {
        return start.add(duration);
    }
    // endregion

    // region | Setters
    public void setStart(TimePoint start) {
        this.start = start;
    }
    public void setDuration(TimePoint duration) {
        this.duration = duration;
    }
    public void setEnd(TimePoint end) {
        this.duration = end.sub(this.start);
    }
    // endregion
}
