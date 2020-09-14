package io.odinjector;

import javax.inject.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class Context implements ContextMarker {
	Map<Class<?>, List<Binding<?>>> contextBindings = new ConcurrentHashMap<>();
	Map<Class<?>, Provider<?>> providers = new ConcurrentHashMap<>();
	Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();


	public abstract void configure(Binder binder);

	void init() {
		configure(new ContextBinder(this));
	}

	@SuppressWarnings("unchecked")
	<T> List<BindingResult<T>> getBindings(InjectionContext<T> injectionContext) {
		List res = (List) (contextBindings.containsKey(injectionContext.clazz)
				? contextBindings.get(injectionContext.clazz).stream().map(b -> BindingResult.of(b, this)).collect(Collectors.toList())
				: Collections.emptyList());
		return res;
	}

	public <T> BindingResult<T> getBinding(InjectionContext<T> injectionContext) {
		List<BindingResult<T>> bindings = getBindings(injectionContext);
		if (bindings.isEmpty()) {
			return BindingResult.empty();
		}
		return bindings.get(0);
	}

	@SuppressWarnings("unchecked")
	public <T> T singleton(Class<T> clazz, Provider<T> provider) {
		return (T)singletons.computeIfAbsent(clazz, c2 -> provider.get());
	}

	public Class<? extends ContextMarker> getMarkedContext() {
		return getClass();
	}
}
