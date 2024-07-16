package flooferland.showbiz.lowlevel;

import flooferland.chirp.safety.Option;
import flooferland.chirp.safety.Result;
import flooferland.showbiz.lowlevel.types.BitInfo;
import flooferland.showbiz.lowlevel.types.DrawerInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class BitChart {
    public static final String path = "RAE_Bit_Chart.csv";
    public static final ArrayList<Entry> chart = read().unwrap();

    public record Entry(String getName, BitInfo getBit, String getType) {}
    
    private BitChart() {}
    
    private static Result<ArrayList<Entry>, String> read() {
        ArrayList<Entry> entries = new ArrayList<>();
        
        // Reading in the bit chart
        try (InputStream bitChartStream = MidiSignalManager.class.getClassLoader().getResourceAsStream(path)) {
            String bitChart = new String(bitChartStream.readAllBytes());
            for (String line : Arrays.stream(bitChart.split("\n")).skip(1).toList()) {
                String[] row = line.split(",");
                if (row.length != 3) continue;
                
                Option<BitInfo> bitDataOpt = BitInfo.fromString(row[1]);
                if (bitDataOpt.letSome() instanceof BitInfo bitInfo) {
                    BitChart.Entry entry = new Entry(row[0], bitInfo, row[2]);
                    entries.add(entry);
                }
            }
        } catch (Exception e) {
            return Result.err(e.toString());
        }
        
        if (entries.isEmpty()) {
            return Result.err("BitChart.read entries array is empty");
        }
        
        return Result.ok(entries);
    }

    /**
     * Gets which drawer an int should represent.
     * Assumes int has the length of both drawers (ex: max 96)
     */
    public static Pair<Integer, DrawerInfo> getBitFromInt(int i) {
        int topDrawerLength = 0;
        for (Entry entry : chart) {
            if (entry.getBit().Drawer.equals(DrawerInfo.Top)) {
                topDrawerLength += 1;
            }
        }
        
        if (i == 0) {
            return Pair.of(0, DrawerInfo.Bottom);
        }
        
        if (i > topDrawerLength) {
            return Pair.of(i - topDrawerLength, DrawerInfo.Bottom);
        } else {
            return Pair.of(topDrawerLength - i, DrawerInfo.Bottom);
        }
    }
}
