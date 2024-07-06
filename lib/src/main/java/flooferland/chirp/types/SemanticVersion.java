package flooferland.chirp.types;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import flooferland.chirp.safety.Option;
import jdk.jfr.Unsigned;

import java.io.IOException;

@JsonSerialize(using = SemanticVersion.Serializer.class)
@JsonDeserialize(using = SemanticVersion.Deserializer.class)
public class SemanticVersion {
    @Unsigned private final int major;
    @Unsigned private final int minor;
    @Unsigned private final int patch;
    
    // region | Constructors
    public SemanticVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public SemanticVersion(int major, int minor) {
        this(major, minor, 0);
    }

    public SemanticVersion(int major) {
        this(major, 0, 0);
    }

    @Override
    public String toString() {
        return String.format("%s.%s.%s", major, minor, patch);
    }
    public static Option<SemanticVersion> fromString(String string) {
        String[] split = string.split("\\.", 3);
        if (split.length == 3) {
            try {
                int major = Integer.parseInt(split[0]);
                int minor = Integer.parseInt(split[1]);
                int patch = Integer.parseInt(split[2]);
                return Option.some(new SemanticVersion(major, minor, patch));
            } catch (NumberFormatException exception) {
                return Option.none();
            }
        }
        
        return Option.none();
    }

    public static class Serializer extends StdSerializer<SemanticVersion> {
        public Serializer() {
            super(SemanticVersion.class);
        }

        @Override
        public void serialize(SemanticVersion version, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(version.toString());
        }
    }

    public static class Deserializer extends StdDeserializer<SemanticVersion> {
        public Deserializer() {
            super(SemanticVersion.class);
        }

        @Override
        public SemanticVersion deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JacksonException {
            JsonNode node = parser.getCodec().readTree(parser);
            Option<SemanticVersion> data = SemanticVersion.fromString(node.asText());
            if (data.letSome() instanceof SemanticVersion version) {
                return version;
            }
            return null;
        }
    }
}
