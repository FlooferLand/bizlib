package flooferland.chirp.safety;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import flooferland.chirp.types.SemanticVersion;

import javax.annotation.Nullable;
import java.io.IOException;

@JsonSerialize(using = Option.Serializer.class)
@JsonDeserialize(using = Option.Deserializer.class)
public final class Option<T> {
    private final boolean hasValue;
    private final @Nullable T value;
    
    private Option(@Nullable T value) {
        this.value = value;
        this.hasValue = value != null;
    }
    private Option() {
        this.value = null;
        this.hasValue = false;
    }
    
    // region | Constructors
    /** Stores a value; stores nothing is the value is <c>null</c> */
    public static <T> Option<T> some(@Nullable T value) {
        return (value == null)
                ? new Option<>()
                : new Option<>(value);
    }
    
    /** Stores nothing */
    public static <T> Option<T> none() {
        return new Option<>();
    }

    /** Stores a value if the condition is true, otherwise it stores nothing */
    public static <T> Option<T> conditional(boolean condition, T onTrue) {
        return condition ? some(onTrue) : none();
    }
    // endregion
    
    // region | Checking the value
    public boolean hasValue() {
        return hasValue && value != null;
    }
    // endregion
    
    // region | Getting the value
    public @Nullable T letSome() {
        return this.value;
    }
    
    public interface IMapSome<T> { T mapper(T value); }
    public Option<T> mapSome(IMapSome<T> map) {
        Option<T> val = some(value);
        return val.letSome() instanceof T v ? some(map.mapper(v)) : none();
    }
    
    public T expect(String message) {
        if (value != null)
            return value;
        else
            throw new RuntimeException(message);
    }

    public T unwrap() {
        assert(value != null);
        return value;
    }
    
    public T unwrapOr(T _default) {
        if (value != null)
            return value;
        else
            return _default;
    }
    // endregion
    
    // region | Jackson
    public static class Serializer<T> extends StdSerializer<Option<T>> {
        public Serializer() {
            this(null);
        }
        protected Serializer(Class<Option<T>> t) {
            super(t);
        }

        @Override
        public void serialize(Option<T> option, JsonGenerator gen, SerializerProvider provider) throws IOException {
            ObjectMapper mapper = (ObjectMapper) gen.getCodec();
            if (option.hasValue()) {
                gen.writeString(mapper.writeValueAsString(option.value));
            }
        }
    }

    public static class Deserializer<T> extends StdDeserializer<Option<T>> {
        public Deserializer() {
            this(null);
        }
        protected Deserializer(Class<Option<T>> t) {
            super(t);
        }

        @Override
        public Option<T> deserialize(JsonParser parser, DeserializationContext ctx) throws IOException, JacksonException {
            JsonNode node = parser.getCodec().readTree(parser);
            //. Option<Option<T>> data = map;
            //. if (data.letSome() instanceof Option<T> version) {
            //.     return version;
            //. }
            return null;
        }
    }
    // endregion
}
