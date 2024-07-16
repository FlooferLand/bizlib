package flooferland.showbiz.lowlevel.transformers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import flooferland.chirp.safety.Result;
import flooferland.showbiz.lowlevel.IShowFormat;
import flooferland.showbiz.lowlevel.MidiSignalManager;
import flooferland.showbiz.lowlevel.show.Metadata;
import flooferland.showbiz.lowlevel.show.ShowData;
import flooferland.showbiz.lowlevel.types.SignalContainer;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.sampled.AudioSystem;
import java.io.*;

// TODO: Make this check the hash of every file inside the archive,
//       make it not re-write files if the file in the archive would have the same hash as the file that'll be written to it

/**
 * File format made around 7z
 */
public class ZshowFormat implements IShowFormat {
    private final String extension = "zshw";
    private final int[] version = { 0, 0, 1 };
    
    public static final YAMLFactory factory = YAMLFactory.builder()
            .enable(YAMLGenerator.Feature.USE_NATIVE_OBJECT_ID)
            .enable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID)
            .disable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .disable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
            .disable(YAMLGenerator.Feature.USE_PLATFORM_LINE_BREAKS)
            .build();
    
    public static final ObjectMapper mapper = new ObjectMapper(factory)
            .enable(JsonParser.Feature.ALLOW_COMMENTS)
            .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
            .disable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
    
    @Override
    public Result<ShowData, String> readFromStream(InputStream fileStream) throws IOException {
        byte[] fileBytes = fileStream.readAllBytes();
        var byteChannel = new SeekableInMemoryByteChannel(fileBytes);

        // Result
        SignalContainer container = null;
        byte[] audio = null;
        byte[] video = null;
        Metadata metadata = null;
        
        // Reading every file in the archive
        try (SevenZFile archive = SevenZFile.builder().setSeekableByteChannel(byteChannel).get()) {
            for (SevenZArchiveEntry entry : archive.getEntries()) {
                InputStream stream = archive.getInputStream(entry);
                System.out.printf("Reading file \"%s\" from 7z%n", entry.getName());
                switch (entry.getName()) {
                    case "signal.midi":
                    case "signal.mid":
                        try {
                            var containerResult = SignalContainer.fromSequenceSignal(MidiSystem.getSequence(stream));
                            if (containerResult.letOk() instanceof SignalContainer sigContainer) {
                                container = sigContainer;
                            }
                        } catch (Exception e) {
                            return Result.err(e.toString());
                        }
                        break;
                    
                    // Audio
                    case "audio.ogg":
                    case "audio.mp3":
                    case "audio.wav":
                        try {
                            audio = AudioSystem.getAudioInputStream(stream).readAllBytes();
                        } catch (Exception e) {
                            return Result.err(e.toString());
                        }
                        break;
                        
                    // Video
                    case "video.webm":
                        // TODO: Implement reading videos for zshow files
                        System.err.println("Reading video is not implemented yet!");
                        break;

                    // Info
                    case "info.yml":
                    case "metadata.yml":
                        metadata = mapper.readValue(stream, Metadata.class);
                        break;
                        
                    // Cover photo / icon
                    case "cover.webp":
                    case "cover.jpg":
                    case "cover.png":
                        // TODO: Implement reading cover/icon images for zshow files
                        System.err.println("Reading cover/icon images is not implemented yet!");
                        break;
                        
                    default:
                        break;
                }
                stream.close();
            }
        }

        // Returning, and errors
        if (container == null) return Result.err("Signal data was null!");
        if (audio == null) return Result.err("Audio data was null!");
        if (metadata == null) return Result.err("Metadata/info data was null!");
        return Result.ok(new ShowData(container, audio, video, metadata));
    }
    
    @Override
    public void writeToStream(ShowData format, OutputStream stream) throws Exception {
        SeekableInMemoryByteChannel memoryByteChannel = new SeekableInMemoryByteChannel();
        
        try (SevenZOutputFile archive = new SevenZOutputFile(memoryByteChannel)) {
            // Metadata file
            make7zEntry(archive, "metadata.yml", file -> {
                byte[] metadata = mapper.writeValueAsBytes(format.Metadata);
                file.write(metadata);
            });

            // Audio file
            // make7zEntry(archive, "audio.wav", file -> {
            //     file.write(format.Audio);
            // });

            // Signal file
            make7zEntry(archive, "signal.mid", file -> {
                var sequence = MidiSignalManager.fromSignal(format.Signal);

                // Writing the output
                if (sequence.letOk() instanceof Sequence seq) {
                    try (var bitties = new ByteArrayOutputStream()) {
                        MidiSystem.write(seq, 1, bitties);
                        file.write(bitties.toByteArray());

                        // Signal debug
                        try (FileOutputStream midiDebug = new FileOutputStream("C:\\Users\\FlooferLand\\Desktop\\rshow_compatibility\\out.mid")) {  // DEBUG ONLY
                            System.out.printf("Wrote to \"%s\"\n", "C:\\Users\\FlooferLand\\Desktop\\rshow_compatibility\\out.mid");
                            midiDebug.write(bitties.toByteArray());
                        }
                        try (FileOutputStream midiDebug = new FileOutputStream("C:\\Users\\FlooferLand\\midiexplorer_home\\midi\\out.mid")) {  // DEBUG ONLY
                            System.out.printf("Wrote to \"%s\"\n", "C:\\Users\\FlooferLand\\midiexplorer_home\\midi\\out.mid");
                            midiDebug.write(bitties.toByteArray());
                        }
                    }
                } else if (sequence.letErr() instanceof String err) {
                    System.err.println(err);
                }
            });
        }
        stream.write(memoryByteChannel.array());
    }

    protected interface I7zEntry { void entry(SevenZOutputFile entry) throws Exception; }
    protected void make7zEntry(SevenZOutputFile archive, String path, I7zEntry func) throws Exception {
        File file = new File(path);
        SevenZArchiveEntry entry = archive.createArchiveEntry(file, file.getName());
        archive.putArchiveEntry(entry);
        func.entry(archive);
        archive.closeArchiveEntry();
    }
}
