package flooferland.showbiz.showformat;

import java.io.InputStream;

public class DataConverter {
    private static byte[] identBase = "SHWID\0".getBytes();
    private static byte[] noContentIdent = "EMPTY_CONTENT\0".getBytes();
    private static byte[] dataEndIdent = { 0x00 };

    public static byte[] makeIdent(byte[] bytes) {
        return (byte[]) Util.concatBytes(identBase, bytes);
    }
    
    private static boolean matchesHeader(byte[] buffer, byte[] header) {
        for (int i = 0; i < header.length; i++) {
            if (buffer[i] != header[i]) {
                return false;
            }
        }
        return true;
    }
    
    public interface IDataReader {
        public void read();
    }
    
    
    public static void Read(InputStream stream, Ident ident, IDataReader dataReader, long length) {
        byte[] identBytes = ident.toBytes();
        // writer.Write(identBytes);
        // writer.Write(MakeInfo(length));
        // dataWriter(writer);
        // writer.Write(DataEndIdent);
        // Console.WriteLine($"Wrote {length} bytes to ident '{ident}'");
    }
}
