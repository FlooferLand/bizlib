package flooferland.showbiz.lowlevel.util;

public final class BToolsMath {
    /** Converts rshw bits to normal TD/BD*/
    public static int translateRshowBit(int bit) {
        return bit - 150;
    }
}
