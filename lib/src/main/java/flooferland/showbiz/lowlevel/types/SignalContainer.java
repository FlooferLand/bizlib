package flooferland.showbiz.lowlevel.types;

import flooferland.chirp.safety.Option;
import flooferland.chirp.safety.Result;
import flooferland.showbiz.lowlevel.BitChart;
import flooferland.showbiz.lowlevel.MidiSignalManager;

import javax.sound.midi.Sequence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO: Refactor this entire class
//       - Stop using Rshow's int arrays for storing signal data
//       - Prepare for potential multi-drawer support (more than 2 top/bottom drawers)
//       - Use events for storing signal data and add in a function that lets you iterate and `switch` over the ID of each event
//       ? Potentially make it so internally there's no concept of drawers (merging all drawers into one mega-drawer)

/** Holds an array of event signals and has some handy methods */
public class SignalContainer {
    private final SignalEvent[] signal;
    
    public SignalEvent[] getEvents() {
        return signal;
    }

    // region | Constructors
    public SignalContainer(SignalEvent[] signal) {
        this.signal = signal;
    }
    
    public static Result<SignalContainer, String> fromSequenceSignal(Sequence sequence) {
        SignalEvent[] events = new SignalEvent[] { };

        Result<SignalContainer, String> result = MidiSignalManager.toSignal(sequence);
        if (result.letOk() instanceof SignalContainer c) {
            events = c.signal;
        } else {
            System.err.println(result.letErr());
        }
        
        // Returning
        return Result.conditional(events.length != 0, new SignalContainer(events), "events.length is 0");
    }
    
    /** Creates a signal container from Rshow raw bytes */
    public static Result<SignalContainer, String> fromRshowSignal(int[] rshwRaw) {
        ArrayList<Integer> topDrawer = new ArrayList<>();
        ArrayList<Integer> bottomDrawer = new ArrayList<>();

        // Rshow's signal data alternates between top and bottom drawer data
        for (int i = 0; i < rshwRaw.length; i++) {
            int value = rshwRaw[i];
            value = BitChart.getBitFromInt(value).getLeft();  // Appears to be broken
            switch (i % 2) {
                case 0 -> topDrawer.add(value);
                case 1 -> bottomDrawer.add(value);
            }
        }

        int[] _topDrawer = topDrawer.stream().mapToInt(i->i).toArray();
        int[] _bottomDrawer = bottomDrawer.stream().mapToInt(i->i).toArray();
        Result<Sequence, String> result = MidiSignalManager.fromRshow(_topDrawer, _bottomDrawer);
        if (result.letOk() instanceof Sequence sequence) {
            // DEBUG
            System.out.printf("RAW:    %s\n", Arrays.toString(rshwRaw));
            System.out.printf("TOP:    %s\n", Arrays.toString(_topDrawer));
            System.out.printf("BOTTOM: %s\n", Arrays.toString(_bottomDrawer));
            
            if (fromSequenceSignal(sequence).letOk() instanceof SignalContainer container) {
                return Result.ok(container);
            } else {
                return Result.err("Failed to make the internal signal object from a MIDI sequence");
            }
        }

        return Result.err("Failed to make a MIDI sequence from the rshow signal (top/bottom drawers)");
    }
    // endregion
    
    /**
     * Used as a last resort to fix corrupted data. <br/>
     * Current fixes: <ul>
     *  <li>If there is no "bit end" event and there is a "bit begin" event, there will be a "bit end" event inserted before it.</li>
     * </ul>
     */
    public SignalContainer fixData() {
        var events = new ArrayList<>(List.of(signal.clone()));
        
        // Bit event fixes
        Option<SignalEvents.BitEvent> lastEventOpt = Option.none();
        for (int i = 0; i < signal.length; i++) {
            if (!(signal[i] instanceof SignalEvents.BitEvent event && lastEventOpt.letSome() instanceof SignalEvents.BitEvent lastEvent)) continue;
            
            // Creates an end note when a note begins if the previous note hasn't ended
            if (lastEvent.State && event.State) {
                events.add(i-1, new SignalEvents.BitEvent(event.TimeStamp, false, lastEvent.Bit));
            }
            
            lastEventOpt = Option.some(event);
        }
        
        return new SignalContainer((SignalEvent[]) events.stream().map(e -> (SignalEvent) e).toArray());
    }
}
