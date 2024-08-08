package flooferland.showbiz.lowlevel.types;

import flooferland.chirp.types.math.TimeLength;
import flooferland.chirp.types.math.TimePoint;

import javax.annotation.Nonnull;
import java.time.LocalTime;
import java.util.HashMap;

/** The type for the data signal events (bits) */
public abstract class SignalEvent {
    @Nonnull public final EventType EventId;
    @Nonnull public final TimePoint TimeStamp;
    @Nonnull public final HashMap<String, Object> ExtraData;
    
    /** The event type. Known in MIDI as "status" */
    public enum EventType {
        BIT,
        OTHER
    }

    // region | Constructors
    public SignalEvent(@Nonnull EventType eventId, @Nonnull TimePoint timeStamp, @Nonnull HashMap<String, Object> extraData) {
        EventId = eventId;
        TimeStamp = timeStamp;
        ExtraData = extraData;
    }
    public SignalEvent(@Nonnull EventType eventId, @Nonnull TimePoint timeStamp) {
        EventId = eventId;
        TimeStamp = timeStamp;
        ExtraData = new HashMap<>();
    }
    // endregion
}
