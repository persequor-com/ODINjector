package io.odinjector;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Provides<RE> {
    RE call(Injector injector);

    static <C, R> Provides<R> of(Class<? extends C> c, Function<C, R> o) {
        return (Provides<R>) (Injector i) -> o.apply(i.getInstance(c));
    }

    static <C, C2, R> Provides<R> of(Class<? extends C> c, Class<C2> c2, BiFunction<C,C2, R> o) {
        return (Injector i) -> o.apply(i.getInstance(c), i.getInstance(c2));
    }

    static <C, C2, C3, R> Provides<R> of(Class<? extends C> c, Class<C2> c2, Class<C3> c3, TriFunction<C,C2,C3, R> o) {
        return (Injector i) -> o.apply(i.getInstance(c), i.getInstance(c2), i.getInstance(c3));
    }

    interface TriFunction<C, C2, C3, R> {
        R apply(C c, C2 c2, C3 c3);
    }
}
