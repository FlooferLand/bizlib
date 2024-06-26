package flooferland.showbiz.showformat;

import java.util.Arrays;
import java.util.HexFormat;

public class Util {
    public static <T> T[] concat(T[] a, T[] b) {
        T[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
    public static byte[] concatBytes(byte[] a, byte[] b) {
        byte[] result = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static String toHexString(byte num) {
        return "0x" + String.format("%02x", num).toUpperCase();
    }
    public static String toHexString(int num) {
        return "0x" + Integer.toHexString(num).toUpperCase();
    }
    
    public static String hexArrayToString(byte[] hex) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < hex.length; i++) {
            builder.append(toHexString(hex[i]));
            if (i+1 < hex.length) {
                builder.append(", ");
            }
        }
        builder.append(']');
        return builder.toString();
    }
}
