package flooferland.showbiz.showformat;

import java.io.InputStream;

public class DataConverter {
    public static byte[] identBase = "BLOCK\0".getBytes();
    public static byte[] noContentIdent = "EMPTY_CONTENT\0".getBytes();
    public static byte[] dataEndIdent = { 0x00 };

    public static byte[] makeIdent(byte[] bytes) {
        return (byte[]) Util.concatBytes(identBase, bytes);
    }
}
