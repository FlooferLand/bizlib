package flooferland.showbiz.lowlevel.types;

import flooferland.chirp.safety.Option;
import flooferland.chirp.safety.Result;
import flooferland.chirp.util.Match;
import flooferland.showbiz.lowlevel.BitChart;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Objects;

// TODO: Make sure all casts from int to short are safe

/**
 * Used to represent a bit, made to replace strings like "10 TD", "20 TD", etc.
 */
public class BitInfo {
    public final int Id;
    public final DrawerInfo Drawer;

    private BitInfo(int id, DrawerInfo drawer) {
        Id = id;
        Drawer = drawer;
    }
    
    public enum Error {
        NegativeBitIdentifier,
        BitIdentifierTooHigh
    }
    
    // region | Constructors
    /**
     * Used to represent a bit, made to replace strings like "10 TD", "20 TD", etc.
     */
    public static Result<BitInfo, Error> of(int id, @Nonnull DrawerInfo drawer) {
        if (id < 0) {
            return Result.err(Error.NegativeBitIdentifier);
        } else if (id > DrawerInfo.MaxDrawerCapacity) {
            return Result.err(Error.BitIdentifierTooHigh);
        }

        return Result.ok(new BitInfo(id, drawer));
    }
    /**
     * Used to represent a bit, made to replace strings like "10 TD", "20 TD", etc.
     * Applies patches to make sure a BitInfo always returns and prints out errors instead of throwing them
     * // TODO: Should probably remove this, make all unwraps of Result throw an error instead of crash, and add some way to associate String error messages with Error enums.
     */
    public static BitInfo ofGuaranteed(int id, @Nonnull DrawerInfo drawer) {
        if (of(id, drawer).letOk() instanceof BitInfo bitInfo) {
            return bitInfo;
        } else if (of(id, drawer).letErr() instanceof Error err) {
            final String fixYoProblem = "Please fix this yourself, as this patch may be removed in the future\n";
            switch (err) {
                case Error.NegativeBitIdentifier -> {
                    System.err.printf("Negative bit ID for '%s', patching this by making the ID absolute.\n%s", toString(id, drawer), fixYoProblem);
                    id = Math.abs(id);
                }
                case Error.BitIdentifierTooHigh -> {
                    System.err.printf("Bit ID is over the limit of '%s' for '%s', patching this by capping the ID.\n%s", DrawerInfo.MaxDrawerCapacity, toString(id, drawer), fixYoProblem);
                    id = DrawerInfo.MaxDrawerCapacity;
                }
            }
        }
            
        return new BitInfo(id, drawer);
    }
    
    public static Option<BitInfo> fromString(String str) {
        str = str.toLowerCase().trim();

        String firstPart = str.substring(0, str.length() - 2).trim();
        String lastPart = str.substring(str.length() - 2).trim();
        Option<DrawerInfo> drawerOpt = switch (lastPart) {
            case "td" -> Option.some(DrawerInfo.Top);
            case "bd" -> Option.some(DrawerInfo.Bottom);
            default -> Option.none();
        };
        
        if (drawerOpt.letSome() instanceof DrawerInfo drawer) {
            byte bitLocation = (byte) NumberUtils.toInt(firstPart, -1);
            if (bitLocation != -1) {
                return Option.some(new BitInfo(bitLocation, drawer));
            }
        }
        
        return Option.none();
    }
    public static BitInfo fromNum(int i) {
        Pair<Integer, DrawerInfo> bitInt = BitChart.getBitFromInt(i);
        return new BitInfo(bitInt.getLeft(), bitInt.getRight());
    }
    // endregion    
    
    // region | toString
    @Override
    public String toString() {
        return toString(Id, Drawer);
    }
    public static String toString(int id, DrawerInfo drawer) {
        return id + " " + drawer;
    }
    // endregion

    @Override
    public boolean equals(Object o) {
        if (o instanceof BitInfo other)
            return Id == other.Id && Drawer == other.Drawer;
        else if (o instanceof String str)
            return equals(fromString(str).unwrapOr(null));
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(Id, Objects.hashCode(Drawer));
    }
}
