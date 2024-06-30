package flooferland.showbiz.showformat.formats;

import flooferland.showbiz.showformat.IShowFormat;
import flooferland.showbiz.showformat.ShowData;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * File format made around 7z
 */
public class ZshowFormat implements IShowFormat {
    // TODO: Make this read from a stream
    @Override
    public ShowData readFromStream(InputStream fileStream) throws IOException {
        byte[] fileBytes = IOUtils.toByteArray(fileStream);
        var byteChannel = new SeekableInMemoryByteChannel(fileBytes);

        try (SevenZFile sevenZFile = SevenZFile.builder().setSeekableByteChannel(byteChannel).get()) {
            for (SevenZArchiveEntry entry : sevenZFile.getEntries()) {
                InputStream stream = sevenZFile.getInputStream(entry);
                System.out.printf("Reading file \"%s\" from 7z%n", entry.getName());
                switch (entry.getName()) {
                    case "audio.wav":
                        // TODO: Read audio stream and stuff
                        break;
                    default:
                        break;
                }
                stream.close();
            }
        }

        return null;
    }

    @Override
    public InputStream writeToStream(ShowData format) {
        return null;
    }
}
