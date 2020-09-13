/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2020-09-12
 */
package io.odinjector;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OdinJector {
	private final Map<Class<? extends Context>, Context> contexts = Collections.synchronizedMap(new LinkedHashMap<>());
	private final Map<Class<? extends Context>, Context> dynamicContexts = new ConcurrentHashMap<>();
	private final Map<InjectionContext, Provider> providers = new ConcurrentHashMap<>();
	private final GlobalContext globalContext;

	private OdinJector() {
		globalContext = new GlobalContext(contexts);
	}

	public static OdinJector create() {
		return new OdinJector();
	}

	public OdinJector addContext(Class<? extends Context> context) {
		contexts.computeIfAbsent(context, c -> {
			try {
				Context contextInstance = context.newInstance();
				contextInstance.init();
				return contextInstance;
			} catch (Exception e) {
				throw new InjectionException(e);
			}
		});
		return this;
	}

	private void addDynamicContext(Class<? extends Context> context) {
		dynamicContexts.computeIfAbsent(context, c -> {
			try {
				Context contextInstance = context.newInstance();
				contextInstance.init();
				return contextInstance;
			} catch (Exception e) {
				throw new InjectionException(e);
			}
		});
	}

	public <T> T getInstance(Class<T> type) {
		return getInstance(InjectionContext.get(new ArrayList<>(), type));
	}

	@SuppressWarnings("unchecked")
	<T> T getInstance(InjectionContext<T> injectionContext) {
		return (T)providers.computeIfAbsent(injectionContext, c -> {
			InjectionSetup<T> setup = setup(injectionContext);

			BindingResult<T> binding = getBoundClass(globalContext, setup.thisInjectionContext);

			Provider<T> provider = binding.binding.getProvider(globalContext, setup.thisInjectionContext, injectionContext, this);

			if (binding.binding.isSingleton()) {
				return () -> binding.context.singleton(injectionContext.clazz, provider);
			} else {
				return provider;
			}
		}).get();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getInstances(InjectionContext<T> injectionContext) {
		return (List<T>)providers.computeIfAbsent(injectionContext, c -> {
			InjectionSetup<T> setup = setup(injectionContext);

			List<BindingResult<T>> bindings = getBoundClasses(globalContext, setup.thisInjectionContext);

			return () -> bindings.stream().map(binding -> binding.binding.getProvider(globalContext, setup.thisInjectionContext, injectionContext, this).get()).collect(Collectors.toList());
		}).get();
	}

	private <T> InjectionSetup<T> setup(InjectionContext<T> injectionContext) {
		Class<T> clazz = injectionContext.clazz;
		Class<? extends Context> contextClass = clazz.isAnnotationPresent(ContextualInject.class)
				? clazz.getAnnotation(ContextualInject.class).value()
				: GlobalContext.class;

		InjectionContext<T> thisInjectionContext = injectionContext.copy();

		if (contextClass != GlobalContext.class) {
			addDynamicContext(contextClass);

			thisInjectionContext.context.add(0, dynamicContexts.get(contextClass));
			if (clazz.isAnnotationPresent(ContextualInject.class) && clazz.getAnnotation(ContextualInject.class).recursive()) {
				injectionContext.context.add(0, dynamicContexts.get(contextClass));
			}
		}

		return new InjectionSetup<>(injectionContext, thisInjectionContext);
	}


	private <T> BindingResult<T> getBoundClass(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBinding(thisInjectionContext);
	}

	private <T> List<BindingResult<T>> getBoundClasses(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBindings(thisInjectionContext);
	}


	private static class InjectionSetup<T> {
		final InjectionContext<T> injectionContext;
		final InjectionContext<T> thisInjectionContext;

		public InjectionSetup(InjectionContext<T> injectionContext, InjectionContext<T> thisInjectionContext) {
			this.injectionContext = injectionContext;
			this.thisInjectionContext = thisInjectionContext;
		}
	}
}
