package io.odinjector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GlobalContext extends Context {
	private final Map<Class<? extends ContextMarker>, Context> registeredContexts;

	public GlobalContext(Map<Class<? extends ContextMarker>, Context> contexts) {
		this.registeredContexts = contexts;
	}

	@Override
	public void configure(Binder binder) {

	}

	@Override
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
		List<Context> list = new ArrayList<>(registeredContexts.values());
		Collections.reverse(list);
		for(Context context : list) {
			List<BindingResult<T>> bindings = context.getBindings(injectionContext);
			if (!bindings.isEmpty()) {
				return bindings;
			}
		}
		return Collections.singletonList(BindingResult.of(ClassBinding.of(injectionContext.clazz), this));
	}
}
