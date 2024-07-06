package flooferland.chirp.util;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Match {
    @FunctionalInterface
    public interface ICase {
        void execute();
    }
    public interface IMultiPredicate<T> {
        boolean test(T arg1, T arg2);
    }
    
    public static class Case {
        public static <T> Pair<Predicate<T>, ICase> of(Predicate<T> predicate, ICase action) {
            return Pair.of(predicate, action);
        }
        public static <T> Pair<IMultiPredicate<T>, ICase> of(IMultiPredicate<T> predicate, ICase action) {
            return Pair.of(predicate, action);
        }
    }

    /** Standard "switch"-like match statement, with some comparison */
    @SafeVarargs
    public static <T> void on(T value, Pair<Predicate<T>, ICase> ... cases) {
        for (Pair<Predicate<T>, ICase> entry : cases) {
            if (entry.getLeft().test(value)) {
                entry.getValue().execute();
                break;
            }
        }
    }
    
    /** Paired switch statement, mainly used for comparing 2 values */
    @SafeVarargs
    public static <T> void paired(Pair<T, T> values, Pair<IMultiPredicate<T>, ICase> ... cases) {
        for (Pair<IMultiPredicate<T>, ICase> entry : cases) {
            if (entry.getLeft().test(values.getLeft(), values.getRight())) {
                entry.getValue().execute();
                break;
            }
        }
    }
}
