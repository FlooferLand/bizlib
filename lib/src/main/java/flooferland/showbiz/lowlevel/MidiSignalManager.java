package flooferland.showbiz.lowlevel;

import flooferland.chirp.safety.Option;
import flooferland.chirp.safety.Result;
import flooferland.chirp.util.Match;
import flooferland.showbiz.lowlevel.types.BitInfo;
import flooferland.showbiz.lowlevel.types.SignalContainer;
import org.apache.commons.lang3.tuple.Pair;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class MidiSignalManager {
    public static final boolean debugPrintOut = false;
    public static final int defaultNote = 60; // Middle C
    public static final int defaultVelocity = 100;
    
    /** Converts MIDI data to signal/intermediate */
    public static Result<SignalContainer, String> midiToSignal(Sequence sequence) {
        // Creating the signal
        ArrayList<Integer> signal = new ArrayList<>();
        try {
            for (Track track : sequence.getTracks()) {
                for (int i = 0; i < track.size(); i++) {
                    MidiEvent event = track.get(i);
                    switch (event.getMessage().getStatus()) {
                        case ShortMessage.NOTE_ON:
                            break;
                        case ShortMessage.NOTE_OFF:
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            return Result.err(e.toString());
        }
        
        return Result.ok(new SignalContainer(signal.stream().mapToInt(i -> i).toArray()));
    }
    
    /** Converts intermediate / rshw signal data into MIDI data */
    public static Result<Sequence, String> signalToMidi(SignalContainer signal) {
        // Creating the MIDI
        Sequence sequence;
        try {
            // Defining messages
            var endOfTrack = new MetaMessage();
            endOfTrack.setMessage(0x2F, new byte[]{}, 0);
        
            // Defining file
            sequence = new Sequence(Sequence.PPQ, 384);
    
            // Grouping tracks
            var groups = new HashMap<String, BitChart.Entry>();
            for (BitChart.Entry entry : BitChart.chart) {
                String[] nameArray = Arrays.stream(entry.getName().split("-", 2))
                        .map(String::trim).toArray(String[]::new);
                if (nameArray.length > 1) {
                    String groupName = nameArray[0].toLowerCase();
                    if (!groups.containsKey(groupName)) {
                        groups.put(groupName, entry);
                    }
                } else {
                    groups.put(String.format("%s (?)", entry.getName()), entry);
                }
            }
            
            // Timeline
            for (var group : groups.entrySet()) {
                String key = group.getKey();
                BitChart.Entry entry = group.getValue();
                
                // Getting the bit's name (excluding the group name)
                String groupName = null;
                String bitName = null;
                String[] nameArray = Arrays.stream(entry.getName().split("-", 2))
                        .map(String::trim).toArray(String[]::new);
                if (nameArray.length > 1) {
                    groupName = nameArray[0];
                    bitName = nameArray[1];
                }
                if (groupName == null || bitName == null) continue;
                
                // Creating the track
                Track track = sequence.createTrack();
                String trackName = groupName;
                track.add(makeTrackNameEvent(trackName));

                // Creating the signal bits/notes
                int trackEvents = track.size();
                createNotes(sequence, track, signal.TopDrawer, BitInfo.Drawer.Top);
                createNotes(sequence, track, signal.BottomDrawer, BitInfo.Drawer.Bottom);
                if (track.size() - trackEvents <= 1) {
                    System.err.printf("Warning! No notes were added to MIDI track \"%s\"!\n", trackName);
                }

                // End of track meta event
                MidiEvent endOfTrackEvent = new MidiEvent(endOfTrack, sequence.getTickLength());
                track.add(endOfTrackEvent);
            }
            
            // Empty signal track just in case
            if (sequence.getTracks().length == 0) {
                Track track = sequence.createTrack();
                track.add(makeTrackNameEvent("Empty"));
                System.err.println("Warning! MIDI was made with no tracks!");
            }
        } catch (InvalidMidiDataException exception) {
            return Result.err("Invalid midi data: %s", exception);
        }
        
        return Result.ok(sequence);
    }
    
    public static MidiEvent makeTrackNameEvent(String name) throws InvalidMidiDataException {
        MetaMessage nameMessage = new MetaMessage();
        nameMessage.setMessage(0x03, name.getBytes(), name.length());
        return new MidiEvent(nameMessage, 0);
    }
    
    public static void createNotes(Sequence sequence, Track track, int[] signal, BitInfo.Drawer drawer) throws InvalidMidiDataException {
        int channel = (drawer == BitInfo.Drawer.Top ? 1 : 0);
        
        int lastValue = 0;
        for (int i = 0; i < signal.length; i++) {
            int value = signal[i];
            // BitInfo bitInfo = BitInfo.fromInt(value);
            ShortMessage bitOn = new ShortMessage(ShortMessage.NOTE_ON, channel, value, defaultVelocity);
            ShortMessage bitOff = new ShortMessage(ShortMessage.NOTE_OFF, channel, value, defaultVelocity);
            
            // Adding notes
            Option<Integer> prevPos = Option.conditional(i != 0, (i - 1) * sequence.getResolution());
            int pos = i * sequence.getResolution();
            final int matchLastValue = lastValue;
            final int matchDebugPos = i;
            Match.paired( Pair.of(lastValue, value),
                    // No note (empty/skip)
                    Match.Case.of( (previous, current) -> (previous == 0 && current == 0),
                            () -> {
                                printIfDebug("..");
                            }
                    ),
                    
                    // Note start (ex: 0 -> 100)
                    Match.Case.of( (previous, current) -> (previous == 0 && current > 0),
                            () -> {
                                printIfDebug("\nStart[pos=%s,channel=%s] = %s", matchDebugPos, channel, value);
                                track.add(new MidiEvent(bitOn, pos));
                            }
                    ),
                    
                    // Note hold (ex: 100 -> 100)
                    Match.Case.of( (previous, current) -> (previous.equals(current) && previous != 0),
                            () -> {
                                printIfDebug("\tHold[pos=%s]", matchDebugPos, channel);
                            }
                    ),
                    
                    // Note change (ex: 100 -> 145)
                    Match.Case.of( (previous, current) -> (!previous.equals(current) && previous != 0 && current != 0),
                            () -> {
                                // Turn off the last note, if there is one
                                if (prevPos.letSome() instanceof Integer prev) {
                                    printIfDebug("Change[pos=%s,channel=%s] = (%s -> %s)", matchDebugPos, channel, matchLastValue, value);
                                    track.add(new MidiEvent(bitOff, prev));
                                } else {
                                    printIfDebug("CStart[pos=%s,channel=%s] = %s", matchDebugPos, channel, value);
                                }
                                track.add(new MidiEvent(bitOn, pos));
                            }
                    ),
                    
                    // Note end (ex: 100 -> 0)
                    Match.Case.of( (previous, current) -> (previous > 0 && current == 0),
                            () -> {
                                printIfDebug("End[pos=%s,channel=%s]\n", matchDebugPos, channel);
                                track.add(new MidiEvent(bitOff, pos));
                            }
                    )
            );

            // Setting the previous value
            lastValue = value;
        }

        // Ensure the last note is turned off if it extends to the end of the signal array
        if (lastValue > 0) {
            int noteEndPos = signal.length * sequence.getResolution();
            ShortMessage bitOff = new ShortMessage(ShortMessage.NOTE_OFF, channel, lastValue, defaultVelocity);
            track.add(new MidiEvent(bitOff, noteEndPos));
        }
    }

    private static <T> void printIfDebug(String s, Object ... args) {
        if (debugPrintOut) {
            System.out.printf(s + '\n', args);
        }
    }
}
