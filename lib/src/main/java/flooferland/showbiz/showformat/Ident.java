package flooferland.showbiz.showformat;

import static flooferland.showbiz.showformat.DataConverter.makeIdent;

public enum Ident {
    Signal,
    Audio,
    Video;

    public static final byte[] signalBytes = makeIdent(new byte[] { 0x1A, /* Reserved */ 0x00 });
    public static final byte[] audioBytes  = makeIdent(new byte[] { 0x2A, /* Reserved */ 0x00 });
    public static final byte[] videoBytes  = makeIdent(new byte[] { 0x3A, /* Reserved */ 0x00 });

    public byte[] toBytes() {
        return switch (this) {
            case Ident.Signal -> signalBytes;
            case Ident.Audio  -> audioBytes;
            case Ident.Video  -> videoBytes;
        };
    }
    
    public static Ident fromBytes(byte[] bytes) {
        if (bytes == signalBytes) {
            return Ident.Signal;
        } else if (bytes == audioBytes) {
            return Ident.Audio;
        } else {
            return Ident.Video;
        }
    }
}
