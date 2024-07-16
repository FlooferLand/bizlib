package flooferland.showbiz.lowlevel.types;

import flooferland.chirp.safety.Result;
import flooferland.chirp.types.math.VectorT;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import java.util.HashMap;

/** The type for the data signal events (bits) */
public abstract class SignalEvent {
    @Nonnull public final EventType EventId;
    @Nonnull public final VectorT Time;
    @Nonnull public final HashMap<String, Object> ExtraData;
    
    /** The event type. Known in MIDI as "status" */
    public enum EventType {
        BIT,
        OTHER
    }

    // region | Constructors
    public SignalEvent(@Nonnull EventType eventId, @Nonnull VectorT time, @Nonnull HashMap<String, Object> extraData) {
        EventId = eventId;
        Time = time;
        ExtraData = extraData;
    }
    public SignalEvent(@Nonnull EventType eventId, @Nonnull VectorT time) {
        EventId = eventId;
        Time = time;
        ExtraData = new HashMap<>();
    }
    // endregion
    
    // region | Overridables
    public Result<MidiEvent, String> toMidi(Sequence sequence, double bpm) {
        throw new NotImplementedException("toMidi not implemented on a child of SignalEvent");
    }

    public static Result<SignalEvent, String> fromMidi(@Nonnull MidiEvent event, @Nonnull VectorT time) {
        throw new NotImplementedException("fromMidi not implemented on a child of SignalEvent");
    }
    // endregion
}
