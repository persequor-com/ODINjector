package io.odinjector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GlobalContext extends Context {
	private final Map<Class<? extends Context>, Context> registeredContexts;

	public GlobalContext(Map<Class<? extends Context>, Context> contexts) {
		this.registeredContexts = contexts;
	}

	@Override
	public void configure(Binder binder) {

	}

	@Override
	<T> List<Binding<T>> getBindings(List<Class<? extends Context>> contexts, Class<T> type) {
		return contexts.stream()
				.filter(registeredContexts::containsKey)
				.map(registeredContexts::get)
				.map(c -> c.getBindings(contexts, type))
				.filter(l -> l.size() > 0)
				.findFirst()
				.orElseGet(() -> getBindings(new ArrayList<>(registeredContexts.keySet()), type));
	}
}
