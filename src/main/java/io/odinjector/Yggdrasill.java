package io.odinjector;

import com.sun.jndi.ldap.Connection;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class Yggdrasill extends Context {
	final Map<Class<?>, Context> contexts = Collections.synchronizedMap(new LinkedHashMap<>());
	private final Map<Class<?>, Context> dynamicContexts = Collections.synchronizedMap(new LinkedHashMap<>());

	public void addContext(Context context) {
		context.init();
		contexts.put(context.getClass(), context);
	}

	public void addDynamicContext(Context context) {
		context.init();
		dynamicContexts.put(context.getMarkedContext(), context);
	}

	@Override
	public void configure(Binder binder) {

	}

	<T> List<BindingResult<T>> getBindings(InjectionContext<T> injectionContext) {
		if (injectionContext.context != null) {
			List<Context> list = new ArrayList<>(injectionContext.context);
			Collections.reverse(list);
			for (Context context : list) {
				List<BindingResult<T>> bindings = context.getBindings(injectionContext);
				if (!bindings.isEmpty()) {
					return bindings;
				}
			}
		}
		List<Context> list = new ArrayList<>(contexts.values());
		Collections.reverse(list);
		for(Context context : list) {
			List<BindingResult<T>> bindings = context.getBindings(injectionContext);
			if (!bindings.isEmpty()) {
				return bindings;
			}
		}
		if (injectionContext.isOptional() && (injectionContext.clazz.isInterface() || Modifier.isAbstract(injectionContext.clazz.getModifiers()))) {
			return Collections.emptyList();
		}
		return Collections.singletonList(BindingResult.of(ClassBinding.of(injectionContext.clazz), this));
	}

	List<? extends Context> getDynamicContexts(List<Class<?>> annotationContexts) {
		return annotationContexts.stream().map(ac -> {
			if (!dynamicContexts.containsKey(ac)) {
				throw new InjectionException("Unable to find a registered dynamic context for: "+ac.getName());
			}
			return dynamicContexts.get(ac);
		}).collect(Collectors.toList());
	}
}
