package io.odinjector;

import java.util.function.Function;

public interface BindingResultModifier<T> {
    BindingKey<? extends T> getSource();
    BindingKey<? extends T> getBound();
    void wrap(Function<T, T> t);
}
