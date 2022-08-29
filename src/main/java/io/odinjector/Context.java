package io.odinjector;

import javax.inject.Provider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public abstract class Context {
	Map<BindingKey<?>, List<Binding<?>>> contextBindings = new ConcurrentHashMap<>();
	Map<BindingKey<?>, Provider<?>> providers = new ConcurrentHashMap<>();
	Map<Class<?>, BindingListener> bindingListeners = new ConcurrentHashMap<>();
	Set<Package> packageBindings = Collections.synchronizedSet(new HashSet<>());
	Map<Class<?>, BindingResultListener> bindingResultListeners = new ConcurrentHashMap<>();

	public abstract void configure(Binder binder);

	void init() {
		configure(new ContextBinder(this));
	}

	@SuppressWarnings("unchecked")
	<T> List<BindingResult<T>> getBindings(InjectionContext<T> injectionContext) {
		bindingListeners.values().forEach(bl -> bl.listen(injectionContext));
		injectionContext.setResultListeners(bindingResultListeners);
		if (!contextBindings.containsKey(injectionContext.getBindingKey()) && packageBindings.contains(injectionContext.getBindingKey().getBoundClass().getPackage())) {
			contextBindings.put(injectionContext.getBindingKey(), Collections.singletonList(ClassBinding.of(injectionContext.getBindingKey(), false)));
		}
		List res = (List) (contextBindings.containsKey(injectionContext.getBindingKey())
			? contextBindings.get(injectionContext.getBindingKey()).stream().map(b -> BindingResult.of(b, this)).collect(Collectors.toList())
			: Collections.emptyList()
		);
		return res;
	}

	public <T> BindingResult<T> getBinding(InjectionContext<T> injectionContext) {
		List<BindingResult<T>> bindings = getBindings(injectionContext);
		if (bindings.isEmpty()) {
			return BindingResult.empty();
		}
		return bindings.get(0);
	}

	public Class<?> getMarkedContext() {
		return getClass();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		return getClass().equals(o.getClass());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	public void addListener(BindingListener listener) {
		bindingListeners.put(listener.getClass(), listener);
	}

	public Map<Class<?>, BindingListener> getBindingListeners() {
		return bindingListeners;
	}

	public void addPackageBinding(Package aPackage) {
		packageBindings.add(aPackage);
	}

	public void addBindingResultListener(BindingResultListener listener) {
		bindingResultListeners.put(listener.getClass(),listener);
	}
}
