package flooferland.showbiz.lowlevel.types;

import java.util.Objects;

/**
 * Wrapper type for bit drawers.
 * Mostly acts like an enum for the classic top/bottom drawers, but it does support several drawers
 */
public class DrawerInfo {
    public static final int MaxDrawerCapacity = 96;
    public static final int MaxPossibleDrawers = 16;
    public static final DrawerInfo Top = new DrawerInfo(1);
    public static final DrawerInfo Bottom = new DrawerInfo(0);

    public final int Number;

    private DrawerInfo(int number) {
        Number = (short) number;
    }

    private DrawerInfo(short number) {
        Number = number;
    }

    public static DrawerInfo of(short number) {
        if (number < 0) {
            System.err.printf("Creation of a drawer of number '%s' was attempted. TIP: Negative drawers won't make your show play backwards\n", number);
        } else if (number > MaxPossibleDrawers) {
            System.err.printf("You can only have %s drawers due to MIDI limitations.\n", MaxPossibleDrawers);
        }
        return new DrawerInfo(number);
    }

    @Override
    public String toString() {
        if (equals(DrawerInfo.Top)) {
            return "TD";
        } else if (equals(DrawerInfo.Bottom)) {
            return "BD";
        } else {
            return Number + "D";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DrawerInfo other) {
            return Number == other.Number;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Number);
    }
}
