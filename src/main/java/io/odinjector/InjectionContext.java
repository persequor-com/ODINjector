package io.odinjector;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface InjectionContext<T> extends InjectionModifier<T> {
    InjectionContextImpl.CurrentContext getCurrentKey();
    Class<T> getClazz();
    List<Context> getContext();
    void addNext(Collection<? extends Context> dynamicContexts, boolean recursive);
    void addToNext(Collection<? extends Context> annotationContexts, boolean recursive);

    InjectionContext<T> copy();
    BindingTarget getTarget();
    boolean isOptional();
    String logOutput();

    <C> InjectionContextImpl<C> nextContextFor(Class<C> parameterType, BindingTarget target);
    void wrap(Function<T, T> t);

    T wrap(T res);

    void setResultListeners(Map<Class<?>, BindingResultListener> bindingResultListeners);

    void applyBindingResultListeners(T res);
}
