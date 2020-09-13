/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2020-09-12
 */
package io.odinjector;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OdinJector {
	private final Map<Class<? extends Context>, Context> contexts = Collections.synchronizedMap(new LinkedHashMap<>());
	private final Map<Class<? extends Context>, Context> dynamicContexts = new ConcurrentHashMap<>();
	private final Map<InjectionContext.CurrentContext, Provider> providers = new ConcurrentHashMap<>();
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
		return (T)providers.computeIfAbsent(injectionContext.getCurrentKey(), c -> {
			setup(injectionContext);

			BindingResult<T> binding = getBoundClass(globalContext, injectionContext);
			setupForBinding(injectionContext, binding);
			System.out.println(injectionContext.logOutput());

			Provider<T> provider = binding.binding.getProvider(globalContext, injectionContext, this);

			if (binding.binding.isSingleton()) {
				return () -> binding.context.singleton(injectionContext.clazz, provider);
			} else {
				return provider;
			}
		}).get();
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> getInstances(InjectionContext<T> injectionContext) {
		return (List<T>)providers.computeIfAbsent(injectionContext.getCurrentKey(), c -> {
			setup(injectionContext);

			List<BindingResult<T>> bindings = getBoundClasses(globalContext, injectionContext);

			return () -> bindings.stream().map(binding -> {
				InjectionContext<T> newInjectionContext = injectionContext.copy();
				setupForBinding(newInjectionContext, binding);
				return binding.binding.getProvider(globalContext, newInjectionContext, this).get();
			}).collect(Collectors.toList());
		}).get();
	}


	private <T> void setupForBinding(InjectionContext<T> injectionContext, BindingResult<T> binding) {
		if (binding.binding.getElementClass().isAnnotationPresent(ContextualInject.class)) {
			ContextualInject annotation = binding.binding.getElementClass().getAnnotation(ContextualInject.class);
			addDynamicContext(annotation.value());
			injectionContext.context.add(0, dynamicContexts.get(annotation.value()));
			injectionContext.addToNext(Collections.singletonList(dynamicContexts.get(annotation.value())), annotation.recursive());
		}
	}

	private <T> void setup(InjectionContext<T> injectionContext) {
		Class<T> clazz = injectionContext.clazz;

		if (clazz.isAnnotationPresent(ContextualInject.class)) {
			Class<? extends Context> contextClass = clazz.getAnnotation(ContextualInject.class).value();
			addDynamicContext(contextClass);

			System.out.println("mu: "+injectionContext.logOutput());
//				ArrayList<Context> current = new ArrayList<>(injectionContext.context);
			injectionContext.context.add(0, dynamicContexts.get(contextClass));

//				System.out.println("Non recursive: "+current.size());
			injectionContext.addNext(Collections.singletonList(dynamicContexts.get(contextClass)), clazz.getAnnotation(ContextualInject.class).recursive());
			System.out.println("nr: "+injectionContext.logOutput());
		}
		System.out.println(injectionContext.logOutput());
	}


	private <T> BindingResult<T> getBoundClass(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBinding(thisInjectionContext);
	}

	private <T> List<BindingResult<T>> getBoundClasses(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBindings(thisInjectionContext);
	}
}
