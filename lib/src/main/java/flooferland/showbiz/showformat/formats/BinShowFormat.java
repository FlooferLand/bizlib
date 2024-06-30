package flooferland.showbiz.showformat.formats;

import flooferland.showbiz.showformat.*;
import flooferland.showbiz.showformat.data.ShowData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

public class BinShowFormat implements IShowFormat {
    /** Parses the binary intermediate format and returns a new ShowFormat */
    @Override
    public ShowData readFromStream(InputStream inputStream) throws IOException {
        // Uncompressed and reading the data
        var stream = new GZIPInputStream(inputStream);

        // File format header
        String formatHeader = new String(stream.readNBytes(8), StandardCharsets.UTF_8);
        if (!formatHeader.equals("SHOWBIN\0")) {
            System.err.println(String.format("ERROR: File header \"%s\" doesn't match the format!", formatHeader));
            return null;
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
            byte[] header = stream.readNBytes(DataConverter.identBase.length);
            if (!Arrays.equals(header, DataConverter.identBase)) {
                System.err.printf("ERROR: Block header \"%s\" doesn't match the format.\nThe length specified in the header of the last section might've been wrong.%n", new String(header, StandardCharsets.UTF_8));
                return null;
            }

            // First block id
            int blockIdBytes = (int) stream.read();
            Ident blockId = Ident.fromHex(blockIdBytes);
            if (blockId == null) {
                System.err.printf(
                        "Block ID doesn't match any known ID: %s\nExamples:\n- Signal section ID: %s\n- Audio section ID: %s\n- Video section ID: %s%n",
                        Util.toHexString(blockIdBytes),
                        Util.toHexString(Ident.signalByte),
                        Util.toHexString(Ident.audioByte),
                        Util.toHexString(Ident.videoByte)
                );
                return null;
            }

            // Block length
            byte[] blockLengthBytes = stream.readNBytes(4);
            int blockLength = ByteBuffer.wrap(blockLengthBytes).getInt();
            stream.skipNBytes(5);

            System.out.printf("%s | %s%n", Util.hexArrayToString(blockLengthBytes), Util.hexArrayToChars(blockLengthBytes));

            // Reading the data
            byte[] data = null;
            if (stream.available() == 1) {
                data = stream.readNBytes(blockLength);
                if (data == null) {
                    System.err.println("ERROR: Data is null");
                    return null;
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
            System.err.println("Signal/audio data is null for an unknown reason");
            return null;
        }
        return new ShowData(signal, audio, video);
    }

    @Override
    public InputStream writeToStream(ShowData format) throws IOException {
        throw new RuntimeException("Not implemented");
    }
}
