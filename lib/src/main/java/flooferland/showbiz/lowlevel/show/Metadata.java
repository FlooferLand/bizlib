package flooferland.showbiz.lowlevel.show;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import flooferland.chirp.safety.Option;
import flooferland.chirp.types.SemanticVersion;

import javax.annotation.Nonnull;

// TODO: Add song data (title + artist)
// TODO: Add show data (show title + characters?)

public class Metadata {
    @JsonProperty("version")
    @JsonSerialize(using = SemanticVersion.Serializer.class)
    @JsonDeserialize(using = SemanticVersion.Deserializer.class)
    public @Nonnull SemanticVersion Version;
    
    // @JsonProperty("song")
    // @JsonSerialize(using = Option.Serializer.class)
    // @JsonDeserialize(using = Option.Deserializer.class)
    // public Option<SongData> Song;

    /** Default constructor */
    public Metadata() {
        Version = new SemanticVersion(0, 1, 0);
        // Song = Option.none();
    }
    
    /** Main constructor */
    public Metadata(@Nonnull SemanticVersion version, SongData song) {
        Version = version;
        // Song = Option.some(song);
    }
    
    public record SongData(String title, String artist, Option<String> album) {}
}
