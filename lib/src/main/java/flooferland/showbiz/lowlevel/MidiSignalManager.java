package flooferland.showbiz.lowlevel;

import flooferland.chirp.safety.Option;
import flooferland.chirp.safety.Result;
import flooferland.chirp.types.math.TimeLength;
import flooferland.chirp.types.math.TimePoint;
import flooferland.chirp.util.Match;
import flooferland.showbiz.lowlevel.types.*;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// TODO: Look over everything in this class, there is important code that was commented out for debugging purposes

public class MidiSignalManager {
    public static final boolean debugPrintOut = false;
    public static final int defaultNote = 60; // Middle C
    public static final int defaultVelocity = 100;
    public static final double signalBPM = 120;
    public static final int resolutionPPQ = 384;

    /**
     * Converts rshw signal data into MIDI data.
     * This is also currently used to convert RSHW data to the internal event system, kill 2 birds with one stone.
     */
    @Nonnull public static Result<Sequence, String> fromRshow(@Nonnull int[] topDrawer, @Nonnull int[] bottomDrawer) {
        System.out.println("Converting rshow to MIDI..");
        
        // Creating the MIDI
        Sequence sequence;
        try {
            // Defining file
            sequence = new Sequence(Sequence.PPQ, resolutionPPQ);

            // DEBUGGING
            /*
            Track testTrack = sequence.createTrack();
            ShortMessage bitOn = new ShortMessage(ShortMessage.NOTE_ON, 0, 60, defaultVelocity);
            ShortMessage bitOff = new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, defaultVelocity);
            testTrack.add(new MidiEvent(bitOn, 1 * sequence.getResolution()));
            testTrack.add(new MidiEvent(bitOff, 20 * sequence.getResolution()));
            */
            
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
            // CHECKME: This might duplicate the data, making several tracks with the same data, which is.. not very good
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
                track.add(Utils.makeTrackNameEvent(0, trackName));
                track.add(Utils.makeBpmChangeEvent(0, signalBPM));
                track.add(Utils.makeInstrumentChangeTrackEvent(0));

                // Creating the signal bits/notes
                int trackNotes = 0;
                trackNotes += Utils.createNotesFromRshow(sequence, track, topDrawer, DrawerInfo.Top);
                trackNotes += Utils.createNotesFromRshow(sequence, track, bottomDrawer, DrawerInfo.Bottom);
                if (trackNotes <= 1) {
                    System.err.printf("Warning! No notes were added to MIDI track \"%s\"!\n", trackName);
                } else {
                    System.out.printf("%s rshw notes added to track \"%s\"!\n", trackNotes, trackName);
                }

                // End of track meta event
                track.add(Utils.makeEndOfDataEvent(sequence.getTickLength()));
            }
            
            // Empty signal track just in case
            if (sequence.getTracks().length == 0) {
                Track track = sequence.createTrack();
                track.add(Utils.makeTrackNameEvent(0, "Empty"));
                System.err.println("Warning! MIDI was made with no tracks!");
            }
        } catch (InvalidMidiDataException exception) {
            return Result.err("Invalid midi data: %s", exception);
        }

        return Result.ok(sequence);
    }
    
    // TODO: Finish this; needed for exporting to rshw
    /** Converts MIDI data to rshow signal */
    @Nonnull public static Result<SignalContainer, String> toRshow(@Nonnull Sequence sequence) {
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
        
        return SignalContainer.fromRshowSignal(signal.stream().mapToInt(i -> i).toArray());
    }

    /**
     * Converts intermediate / internal signal data into MIDI data.
     * Very important, as it is used to write signal data to file
     */
    @Nonnull public static Result<Sequence, String> fromSignal(@Nonnull SignalContainer signalContainer) {
        // Creating the MIDI
        Sequence sequence;
        try {
            // Defining messages
            var endOfTrack = new MetaMessage();
            endOfTrack.setMessage(0x2F, new byte[]{}, 0);

            // Defining file
            sequence = new Sequence(Sequence.PPQ, resolutionPPQ);

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
                String[] nameArray = Arrays.stream(entry.getName().split("-", 2)).map(String::trim).toArray(String[]::new);
                if (nameArray.length > 1) {
                    groupName = nameArray[0];
                    bitName = nameArray[1];
                }
                if (groupName == null || bitName == null) continue;

                // Creating the track
                Track track = sequence.createTrack();
                String trackName = groupName;
                track.add(Utils.makeTrackNameEvent(0, trackName));
                track.add(Utils.makeBpmChangeEvent(0, signalBPM));

                // Creating the signal bits/notes
                int trackEventsBefore = track.size();
                for (SignalEvent signalEvent : signalContainer.getEvents()) {
                    if (SignalEvents.toMidi(signalEvent, sequence, signalBPM).letOk() instanceof MidiEvent event) {
                        track.add(event);
                    }
                }
                int trackNotes = track.size() - trackEventsBefore;
                if (trackNotes <= 1) {
                    System.err.printf("Warning! No notes were added to MIDI track \"%s\"!\n", trackName);
                } else {
                    System.out.printf("%s signal notes added to track \"%s\"!\n", trackNotes, trackName);
                }

                // End of track meta event
                // MidiEvent endOfTrackEvent = new MidiEvent(endOfTrack, sequence.getTickLength());
                // track.add(endOfTrackEvent);
            }

            // Empty signal track just in case
            if (sequence.getTracks().length == 0) {
                Track track = sequence.createTrack();
                track.add(Utils.makeTrackNameEvent(0, "Empty"));
                System.err.println("Warning! MIDI was made with no tracks!");
            }
        } catch (InvalidMidiDataException exception) {
            return Result.err("Invalid midi data: %s", exception);
        }

        return Result.ok(sequence);
    }
    
    // TODO: Fix this; problematic
    //       Doesn't convert rshow data to internal event data correctly
    /** Converts MIDI signal data into intermediate / internal signal data */
    @Nonnull public static Result<SignalContainer, String> toSignal(@Nonnull Sequence sequence) {
        ArrayList<SignalEvent> events = new ArrayList<>();
        for (Track track : sequence.getTracks()) {
            // Skip empty tracks
            if (track.size() == 0) {
                continue;
            }
            
            int eventsBefore = events.size();
            for (int i = 0; i < track.size(); i++) {
                MidiEvent midiEvent = track.get(i);
                if (sequence.getDivisionType() != Sequence.PPQ)
                    return Result.err("Only the PPQ division type is supported for now");

                // Get the time position of the current event
                TimePoint time = TimePoint.ofMidiTicks(sequence, midiEvent.getTick(), MidiSignalManager.signalBPM);

                // Adding the event
                Result midi = SignalEvents.fromMidi(midiEvent, time);
                if (midi.letOk() instanceof SignalEvent ev) {
                    events.add(ev);
                } else if (midi.letErr() instanceof String error) {
                    System.err.println(error);
                }
            }
            
            if (events.size() == eventsBefore) {
                return Result.err("toSignal: Didn't add anything to the track, even though it should've");
            }
        }
        
        if (sequence.getTracks().length == 0) {
            return Result.err("toSignal: Sequence has no tracks");
        }

        SignalContainer container = new SignalContainer(events.toArray(SignalEvent[]::new));
        return Result.ok(container);
    }

   public static class Utils {
       @Nonnull
       public static MidiEvent makeTrackNameEvent(long tick, @Nonnull String name) throws InvalidMidiDataException {
           MetaMessage nameMessage = new MetaMessage();
           nameMessage.setMessage(0x03, name.getBytes(), name.length());
           return new MidiEvent(nameMessage, tick);
       }

       @Nonnull
       public static MidiEvent makeEndOfDataEvent(long tick) throws InvalidMidiDataException {
           MetaMessage endOfDataMessage = new MetaMessage();
           endOfDataMessage.setMessage(0x2F, new byte[] {}, 0);
           return new MidiEvent(endOfDataMessage, tick);
       }

       @Nonnull
       public static MidiEvent makeBpmChangeEvent(long tick, double bpm) throws InvalidMidiDataException {
           int mpq = (int) (60000000 / bpm); // Microseconds per quarter note
           MetaMessage tempoMessage = new MetaMessage();
           byte[] data = {  // Thank you ChatGPT
                   (byte) ((mpq >> 16) & 0xFF),
                   (byte) ((mpq >> 8) & 0xFF),
                   (byte) (mpq & 0xFF)
           };
           tempoMessage.setMessage(0x51, data, data.length);
           return new MidiEvent(tempoMessage, tick);
       }

       @Nonnull
       public static MidiEvent makeInstrumentChangeTrackEvent(long tick) throws InvalidMidiDataException {
           ShortMessage tempoMessage = new ShortMessage();
           tempoMessage.setMessage(ShortMessage.PROGRAM_CHANGE, 0, 0);
           return new MidiEvent(tempoMessage, tick);
       }

       public static int createNotesFromRshow(@Nonnull Sequence sequence, @Nonnull Track track, @Nonnull int[] signal, @Nonnull DrawerInfo drawer) throws InvalidMidiDataException {
           int channel = (drawer == DrawerInfo.Top ? 1 : 0);

           int oldNotesCount = track.size();
           int lastValue = 0;
           for (int i = 0; i < signal.length; i++) {
               int value = signal[i];
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
           
           return (track.size() - oldNotesCount) / (2 /* on/off notes both count*/);
       }

       private static <T> void printIfDebug(String s, Object ... args) {
           if (debugPrintOut) {
               System.out.printf(s + '\n', args);
           }
       }
   }
}
