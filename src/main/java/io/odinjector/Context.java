package io.odinjector;

import javax.inject.Provider;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Context {
	Map<Class<?>, List<Binding<?>>> contextBindings = new ConcurrentHashMap<>();
	Map<Class<?>, Provider<?>> providers = new ConcurrentHashMap<>();
	Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();


	public abstract void configure(Binder binder);

	void init() {
		configure(new ContextBinder(this));
	}

	@SuppressWarnings("unchecked")
	<T> List<Binding<T>> getBindings(List<Class<? extends Context>> contexts, Class<T> type) {
		if (!contexts.contains(getClass())) {
			return Collections.emptyList();
		}
		List res = (List) (contextBindings.containsKey(type)
				? contextBindings.get(type)
				: Collections.singletonList(ClassBinding.of(type)));
		return res;
	}

	public <T> Binding<T> getBinding(List<Class<? extends Context>> contexts, Class<T> parameterType) {
		return getBindings(contexts, parameterType).get(0);
	}

	@SuppressWarnings("unchecked")
	public <T> T singleton(Class<T> clazz, Provider<T> provider) {
		return (T)singletons.computeIfAbsent(clazz, c2 -> provider.get());
	}
}
