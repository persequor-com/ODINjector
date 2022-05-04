package io.odinjector;

import java.util.function.Function;

public interface InjectionModifier<T> {
    void wrap(Function<T, T> t);
    BindingTarget getTarget();
    Class<T> getClazz();
}
