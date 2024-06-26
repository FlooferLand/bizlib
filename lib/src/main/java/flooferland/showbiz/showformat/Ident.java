package flooferland.showbiz.showformat;

import javax.annotation.Nullable;

public enum Ident {
    Signal,
    Audio,
    Video;

    public static final int signalByte = 0xA0;
    public static final int audioByte = 0xB0;
    public static final int videoByte = 0xC0;

    public int toHex() {
        return switch (this) {
            case Ident.Signal -> signalByte;
            case Ident.Audio  -> audioByte;
            case Ident.Video  -> videoByte;
        };
    }
    
    public static @Nullable Ident fromHex(int b) {
        if (b == signalByte) {
            return Ident.Signal;
        } else if (b == audioByte) {
            return Ident.Audio;
        } else if (b == videoByte) {
            return Ident.Video;
        }
        
        return null;
    }
}
