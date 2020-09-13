/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2020-09-12
 */
package io.odinjector;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OdinJector {
	private Map<Class<? extends Context>, Context> contexts = new ConcurrentHashMap<>();
	private Map<InjectionContext, Provider> providers = new ConcurrentHashMap<>();
	private GlobalContext globalContext;

	private OdinJector() {
		globalContext = new GlobalContext(contexts);
	}

	public static OdinJector create() {
		return new OdinJector();
	}

	public OdinJector addContext(Class<? extends Context> context) {
		try {
			Context contextInstance = context.newInstance();
			contextInstance.init();
			contexts.put(context, contextInstance);
		} catch (Exception e) {
			throw new InjectionException(e);
		}

		return this;
	}

	public <T> T getInstance(Class<T> type) {
		return getInstance(InjectionContext.get(Arrays.asList(GlobalContext.class), type));
	}

	@SuppressWarnings("unchecked")
	<T> T getInstance(InjectionContext<T> injectionContext) {
		return (T)providers.computeIfAbsent(injectionContext, c -> {
			InjectionSetup<T> setup = setup(injectionContext);

			Binding<T> binding = getBoundClass(setup.context, setup.thisInjectionContext);

			Provider<T> provider = binding.getProvider(setup.context, setup.thisInjectionContext, injectionContext, this);



			if (binding.isSingleton()) {
				return () -> setup.context.singleton(injectionContext.clazz, provider);
			} else {
				return provider;
			}
		}).get();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getInstances(InjectionContext<T> injectionContext) {
		return (List<T>)providers.computeIfAbsent(injectionContext, c -> {
			InjectionSetup<T> setup = setup(injectionContext);

			List<Binding<T>> bindings = getBoundClasses(setup.context, setup.thisInjectionContext);

			return () -> bindings.stream().map(binding -> binding.getProvider(setup.context, setup.thisInjectionContext, injectionContext, this).get()).collect(Collectors.toList());
		}).get();
	}

	private <T> InjectionSetup<T> setup(InjectionContext<T> injectionContext) {
		Class<T> clazz = injectionContext.clazz;
		Class<? extends Context> contextClass = clazz.isAnnotationPresent(ContextualInject.class)
				? clazz.getAnnotation(ContextualInject.class).value()
				: GlobalContext.class;

		InjectionContext<T> thisInjectionContext = injectionContext.copy();
		if (contextClass != GlobalContext.class) {
			thisInjectionContext.context.add(0, contextClass);
			if (clazz.isAnnotationPresent(ContextualInject.class) && clazz.getAnnotation(ContextualInject.class).recursive()) {
				injectionContext.context.add(0, contextClass);
			}
		}
		Context context = contexts.containsKey(contextClass)
				? new FallbackContext(globalContext, Collections.singletonList(contexts.get(contextClass)))
				: globalContext;

		return new InjectionSetup<>(context, injectionContext, thisInjectionContext);
	}


	private <T> Binding<T> getBoundClass(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBinding(thisInjectionContext.context, thisInjectionContext.clazz);
	}

	private <T> List<Binding<T>> getBoundClasses(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBindings(thisInjectionContext.context, thisInjectionContext.clazz);
	}


	private static class InjectionSetup<T> {
		final Context context;
		final InjectionContext<T> injectionContext;
		final InjectionContext<T> thisInjectionContext;

		public InjectionSetup(Context context, InjectionContext<T> injectionContext, InjectionContext<T> thisInjectionContext) {
			this.context = context;
			this.injectionContext = injectionContext;
			this.thisInjectionContext = thisInjectionContext;
		}
	}
}
