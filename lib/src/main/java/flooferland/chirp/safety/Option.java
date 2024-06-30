package flooferland.chirp.safety;

import javax.annotation.Nullable;

public final class Option<T> {
    private final boolean hasValue;
    private final @Nullable T value;
    
    // region | Constructors
    private Option(@Nullable T value) {
        this.value = value;
        this.hasValue = value != null;
    }
    private Option() {
        this.value = null;
        this.hasValue = false;
    }
    public static <T> Option<T> some(@Nullable T value) {
        return (value == null)
                ? new Option<>()
                : new Option<>(value);
    }
    public static <T> Option<T> none() {
        return new Option<>();
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
}
