package flooferland.showbiz.showformat.formats;

import flooferland.showbiz.showformat.IShowFormat;
import flooferland.showbiz.showformat.ShowData;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * File format made around 7z
 */
public class ZshowFormat implements IShowFormat {
    // TODO: Make this read from a stream
    @Override
    public ShowData readFromStream(InputStream fileStream) throws IOException {
        SevenZFile sevenZFile = SevenZFile.builder().get();

        for (SevenZArchiveEntry entry : sevenZFile.getEntries()) {
            InputStream stream = sevenZFile.getInputStream(entry);
            switch (entry.getName()) {
                case "audio.wav":
                    // TODO: Read audio stream and stuff
                    break;
                default:
                    break;
            }
            stream.close();
        }

        sevenZFile.close();
        return null;
    }

    @Override
    public InputStream writeToStream(ShowData format) {
        throw new RuntimeException("Not implemented");
    }
}
