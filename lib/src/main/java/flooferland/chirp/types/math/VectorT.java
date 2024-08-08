package flooferland.chirp.types.math;

// TODO: Should maybe store the end instead of the duration internally

import javax.annotation.Nonnull;

/**
 * Like a Vector2, but with time!
 * Holds a start time and a duration; essentially just stores a length in time.
 */
public final class VectorT {
    @Nonnull private TimeLength start;
    @Nonnull private TimeLength duration;
    
    private VectorT(@Nonnull TimeLength start, @Nonnull TimeLength duration) {
        this.start = start;
        this.duration = duration;
    }
    
    // region | Constructors
    public static VectorT ofStartEnd(@Nonnull TimeLength start, @Nonnull TimeLength end) {
        return new VectorT(start, end.sub(start));
    }
    public static VectorT ofDuration(@Nonnull TimeLength start, @Nonnull TimeLength duration) {
        return new VectorT(start, duration);
    }
    // endregion

    // region | Getters
    public TimeLength getStart() {
        return start;
    }
    public TimeLength getDuration() {
        return start;
    }
    public TimeLength getEnd() {
        return start.add(duration);
    }
    // endregion

    // region | Setters
    public void setStart(TimeLength start) {
        this.start = start;
    }
    public void setDuration(TimeLength duration) {
        this.duration = duration;
    }
    public void setEnd(TimeLength end) {
        this.duration = end.sub(this.start);
    }
    // endregion
}
