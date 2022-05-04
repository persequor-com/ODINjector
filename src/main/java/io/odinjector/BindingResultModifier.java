package io.odinjector;

import java.util.function.Function;

public interface BindingResultModifier<T> {
    Class<? extends T> getSourceClass();
    Class<? extends T> getBoundClass();
    void wrap(Function<T, T> t);
}
