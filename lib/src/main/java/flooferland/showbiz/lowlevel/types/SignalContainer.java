package flooferland.showbiz.lowlevel.types;

import flooferland.chirp.safety.Result;
import flooferland.showbiz.lowlevel.BitChart;
import flooferland.showbiz.lowlevel.MidiSignalManager;

import javax.sound.midi.Sequence;
import java.util.ArrayList;
import java.util.Arrays;

// TODO: Refactor this entire class
//       - Stop using Rshow's int arrays for storing signal data
//       - Prepare for potential multi-drawer support (more than 2 top/bottom drawers)
//       ? Potentially make it so internally there's no concept of drawers (merging all drawers into one mega-drawer)

public class SignalContainer {
    public final int[] Raw;
    public final int[] TopDrawer;
    public final int[] BottomDrawer;

    /** Converts rshw signal data into intermediate signal data */
    public SignalContainer(Sequence sequence) {
        SignalContainer container;
        Result<SignalContainer, String> result = MidiSignalManager.midiToSignal(sequence);
        if (result.letOk() instanceof SignalContainer c) {
            container = c;
        } else {
            System.err.println(result.letErr());
            container = new SignalContainer(new int[] {});
        }
                
        Raw = container.Raw;
        TopDrawer = container.TopDrawer;
        BottomDrawer = container.BottomDrawer;
    }

        /** Converts rshw signal data into intermediate signal data */
    public SignalContainer(int[] rshwRaw) {
        ArrayList<Integer> topDrawer = new ArrayList<>();
        ArrayList<Integer> bottomDrawer = new ArrayList<>();

        for (int i = 0; i < rshwRaw.length; i++) {
            int value = rshwRaw[i];
            value = BitChart.getBitFromInt(value).getLeft();
            switch (i % 2) {
                case 0 -> topDrawer.add(value);
                case 1 -> bottomDrawer.add(value);
            }
        }
        
        Raw = rshwRaw;
        TopDrawer = topDrawer.stream().mapToInt(i->i).toArray();
        BottomDrawer = bottomDrawer.stream().mapToInt(i->i).toArray();
    
        // DEBUG
        System.out.printf("RAW:    %s\n", Arrays.toString(Raw));
        System.out.printf("TOP:    %s\n", Arrays.toString(TopDrawer));
        System.out.printf("BOTTOM: %s\n", Arrays.toString(BottomDrawer));
    }
}
