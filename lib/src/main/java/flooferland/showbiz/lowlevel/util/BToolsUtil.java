package flooferland.showbiz.lowlevel.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class BToolsUtil {
    // region | Bytes, hex, arrays, etc
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
    
    public static String hexArrayToChars(byte[] hex) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < hex.length; i++) {
            if (i+1 < hex.length) {
                builder.append(ByteBuffer.wrap(new byte[] { hex[i], hex[i+1] }).getChar());
            }
            if (i+1 < hex.length) {
                builder.append(", ");
            }
        }
        builder.append(']');
        return builder.toString();
    }
    // endregion
    
    // region | Other
    
    // endregion
}
