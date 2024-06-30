package flooferland.showbiz.showformat.formats;

import flooferland.showbiz.showformat.IShowFormat;
import flooferland.showbiz.showformat.data.ShowData;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * File format made around 7z
 */
public class ZshowFormat implements IShowFormat {
    private final String extension = "zshw";
    private final int[] version = { 0, 0, 1 };
    
    // TODO: Make this read from a stream
    @Override
    public ShowData readFromStream(InputStream fileStream) throws IOException {
        byte[] fileBytes = IOUtils.toByteArray(fileStream);
        var byteChannel = new SeekableInMemoryByteChannel(fileBytes);

        try (SevenZFile archive = SevenZFile.builder().setSeekableByteChannel(byteChannel).get()) {
            for (SevenZArchiveEntry entry : archive.getEntries()) {
                InputStream stream = archive.getInputStream(entry);
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
    public InputStream writeToStream(ShowData format) throws IOException  {
        File f = new File("");
        try (SevenZOutputFile archive = new SevenZOutputFile(f)) {
            // Metadata file
            File metadataFile = new File("metadata.yml");
            SevenZArchiveEntry entry = archive.createArchiveEntry(metadataFile, metadataFile.getName());
            archive.putArchiveEntry(entry);
            try (SeekableInMemoryByteChannel channel = new SeekableInMemoryByteChannel(format.))
            IOUtils.copy()
        }
        
        return null;
    }
}
