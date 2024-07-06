package flooferland.chirp.safety;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class Result<TOk, TErr> {
    private final boolean hasValue;
    private final @Nullable TOk value;
    private final @Nullable TErr error;
    
    public boolean hasValue() {
        return this.hasValue && this.value != null;
    }
    
    // region | Constructors
    private Result(@Nullable TOk value, @Nullable TErr error) {
        this.value = value;
        this.error = error;
        this.hasValue = value != null;
    }
    public static <TOk, TErr> Result<TOk, TErr> ok(@Nullable TOk value) {
        return new Result<>(value, null);
    }
    public static <TOk, TErr> Result<TOk, TErr> err(@Nullable TErr error) {
        return new Result<>(null, error);
    }public static <TOk> Result<TOk, String> err(@Nonnull String formatErr, Object ... args) {
        return new Result<>(null, String.format(formatErr, args));
    }
    // endregion
    
    // region | Getting the value
    public @Nullable TOk letOk() {
        return this.value;
    }
    public @Nullable TErr letErr() {
        return this.error;
    }
    
    public interface IMapSome<TOk, TErr> { Result<TOk, TErr> mapper(TOk value); }
    public Result<TOk, TErr> mapOk(IMapSome<TOk, TErr> map) {
        if (hasValue())
            return map.mapper(value);
        else
            return err(error);
    }
    
    public TOk expect(String message) {
        if (hasValue())
            return value;
        else
            throw new RuntimeException(message);
    }

    public TOk unwrap() {
        if (hasValue())
            return value;
        else
            throw new RuntimeException(String.valueOf(error));
    }
    
    public TOk unwrapOr(TOk _default) {
        if (value != null)
            return value;
        else
            return _default;
    }
    // endregion
}
