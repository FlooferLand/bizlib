package flooferland.showbiz.lowlevel.transformers;

import flooferland.chirp.safety.Result;
import flooferland.showbiz.lowlevel.*;
import flooferland.showbiz.lowlevel.show.ShowData;
import flooferland.showbiz.lowlevel.util.BToolsUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

public class BinShowFormat implements IShowFormat {
    public static byte[] identBase = "BLOCK\0".getBytes();
    public static byte[] noContentIdent = "EMPTY_CONTENT\0".getBytes();
    public static byte[] dataEndIdent = { 0x00 };

    public static byte[] makeIdent(byte[] bytes) {
        return (byte[]) BToolsUtil.concatBytes(identBase, bytes);
    }

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
    
    /** Parses the binary intermediate format and returns a new ShowFormat */
    @Override
    public Result<ShowData, String> readFromStream(InputStream inputStream) throws IOException {
        // Uncompressed and reading the data
        var stream = new GZIPInputStream(inputStream);

        // File format header
        String formatHeader = new String(stream.readNBytes(8), StandardCharsets.UTF_8);
        if (!formatHeader.equals("SHOWBIN\0")) {
            return Result.err("ERROR: File header \"%s\" doesn't match the format!%n", formatHeader);
        }

        // File metadata
        short version = ByteBuffer.wrap(stream.readNBytes(2)).getShort();
        System.out.printf("Read version '%s' of the format.%n", version);
        stream.skipNBytes(6);

        // Parsing
        @Nonnull int[] signal = null;
        @Nonnull byte[] audio = null;
        @Nullable byte[] video = null;
        for (int i = 0; i < 2; i++) {
            // First block header
            byte[] header = stream.readNBytes(identBase.length);
            if (!Arrays.equals(header, identBase)) {
                return Result.err("ERROR: Block header \"%s\" doesn't match the format.\nThe length specified in the header of the last section might've been wrong.%n", new String(header, StandardCharsets.UTF_8));
            }

            // First block id
            int blockIdBytes = (int) stream.read();
            Ident blockId = Ident.fromHex(blockIdBytes);
            if (blockId == null) {
                System.err.printf(
                        "Block ID doesn't match any known ID: %s\nExamples:\n- Signal section ID: %s\n- Audio section ID: %s\n- Video section ID: %s%n",
                        BToolsUtil.toHexString(blockIdBytes),
                        BToolsUtil.toHexString(Ident.signalByte),
                        BToolsUtil.toHexString(Ident.audioByte),
                        BToolsUtil.toHexString(Ident.videoByte)
                );
                return null;
            }

            // Block length
            byte[] blockLengthBytes = stream.readNBytes(4);
            int blockLength = ByteBuffer.wrap(blockLengthBytes).getInt();
            stream.skipNBytes(5);

            System.out.printf("%s | %s%n", BToolsUtil.hexArrayToString(blockLengthBytes), BToolsUtil.hexArrayToChars(blockLengthBytes));

            // Reading the data
            byte[] data = null;
            if (stream.available() == 1) {
                data = stream.readNBytes(blockLength);
                if (data == null) {
                    return Result.err("ERROR: Data is null");
                }
                if (stream.available() == 1) {
                    stream.skipNBytes(1);
                }
            }

            // Adding the block
            switch (blockId) {
                case Signal:
                    // TODO: LOAD IN SIGNAL DATA FROM INTERMEDIATE FORMAT (convert byte[] to int[])
                    // signal = data;
                    break;
                case Audio:
                    audio = Objects.requireNonNull(data);
                    break;
                case Video:
                    if (data != null)
                        video = data;
                    else
                        video = null;
                    break;
            }
            System.out.printf("Finished reading BlockID '%s'%n%n", blockId);
        }

        // Returning
        if (signal == null || audio == null) {
            return Result.err("Signal/audio data is null for an unknown reason");
        }
        return ShowData.fromRshowSignal(signal, audio, video);
    }

    @Override
    public void writeToStream(ShowData format, OutputStream stream) throws IOException {
        throw new RuntimeException("Not implemented");
    }
}
