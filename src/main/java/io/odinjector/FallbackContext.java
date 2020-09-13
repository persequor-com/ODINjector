package io.odinjector;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FallbackContext extends Context {
	private GlobalContext globalContext;
	private Map<Class<? extends Context>, Context> localContexts;

	public FallbackContext(GlobalContext globalContext, List<Context> localContexts) {
		this.globalContext = globalContext;
		this.localContexts  = new ConcurrentHashMap<>(localContexts.stream().collect(Collectors.toMap(Context::getClass, c -> c)));
	}

	@Override
	public void configure(Binder binder) {

	}

	@Override
	@SuppressWarnings("unchecked")
	<T> List<Binding<T>> getBindings(List<Class<? extends Context>> contexts, Class<T> type) {
		return (List<Binding<T>>)contexts.stream()
				.filter(localContexts::containsKey)
				.map(localContexts::get)
				.filter(c -> c.contextBindings.containsKey(type))
				.map(c -> (List)c.contextBindings.get(type))
				.findFirst()
				.filter(l -> l.size() > 0)
				.orElseGet(() -> globalContext.getBindings(contexts, type));
	}
}
