package flooferland.showbiz.lowlevel.types;

import flooferland.chirp.safety.Option;
import flooferland.showbiz.lowlevel.BitChart;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Objects;

/** Sometimes used to represent bits */
public class BitInfo {
    public final int id;
    public final Drawer drawer;

    public enum Drawer { Top, Bottom }
    
    // region | Constructors
    public BitInfo(int id, Drawer drawer) {
        this.id = id;
        this.drawer = drawer;
    }
    public static Option<BitInfo> fromString(String str) {
        str = str.toLowerCase().trim();

        String firstPart = str.substring(0, str.length() - 2).trim();
        String lastPart = str.substring(str.length() - 2).trim();
        Option<Drawer> drawerOpt = switch (lastPart) {
            case "td" -> Option.some(Drawer.Top);
            case "bd" -> Option.some(Drawer.Bottom);
            default -> Option.none();
        };
        
        if (drawerOpt.letSome() instanceof Drawer drawer) {
            int bitLocation = NumberUtils.toInt(firstPart, -1);
            if (bitLocation != -1) {
                return Option.some(new BitInfo(bitLocation, drawer));
            }
        }
        
        return Option.none();
    }
    public static BitInfo fromInt(int i) {
        Pair<Integer, Drawer> bitInt = BitChart.getBitFromInt(i);
        return new BitInfo(bitInt.getLeft(), bitInt.getRight());
    }
    // endregion    
    
    @Override
    public String toString() {
        String drawerStr = switch (drawer) {
            case Drawer.Top -> "TD";
            case Drawer.Bottom -> "BD";
        };
        return id + " " +  drawerStr;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BitInfo other)
            return id == other.id && drawer == other.drawer;
        else if (o instanceof String str)
            return equals(fromString(str).unwrapOr(null));
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, drawer);
    }
}
