package flooferland.chirp.types.math;

// TODO: Should maybe store the end instead of the duration internally

import javax.annotation.Nonnull;

/**
 * Like a Vector2, but with time!
 * Holds a start time and a duration; essentially just stores a length in time.
 */
public final class VectorT {
    @Nonnull private TimeFrame start;
    @Nonnull private TimeFrame duration;
    
    private VectorT(@Nonnull TimeFrame start, @Nonnull TimeFrame duration) {
        this.start = start;
        this.duration = duration;
    }
    
    // region | Constructors
    public static VectorT ofStartEnd(@Nonnull TimeFrame start, @Nonnull TimeFrame end) {
        return new VectorT(start, end.sub(start));
    }
    public static VectorT ofDuration(@Nonnull TimeFrame start, @Nonnull TimeFrame duration) {
        return new VectorT(start, duration);
    }
    // endregion

    // region | Getters
    public TimeFrame getStart() {
        return start;
    }
    public TimeFrame getDuration() {
        return start;
    }
    public TimeFrame getEnd() {
        return start.add(duration);
    }
    // endregion

    // region | Setters
    public void setStart(TimeFrame start) {
        this.start = start;
    }
    public void setDuration(TimeFrame duration) {
        this.duration = duration;
    }
    public void setEnd(TimeFrame end) {
        this.duration = end.sub(this.start);
    }
    // endregion
}
