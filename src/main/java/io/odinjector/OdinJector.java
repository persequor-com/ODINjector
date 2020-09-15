/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2020-09-12
 */
package io.odinjector;

import javax.inject.Provider;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class OdinJector {
	private final Map<Class<?>, Context> contexts = Collections.synchronizedMap(new LinkedHashMap<>());
	private final Map<Class<?>, Context> dynamicContexts = Collections.synchronizedMap(new LinkedHashMap<>());
	private final Map<InjectionContext.CurrentContext, Provider> providers = Collections.synchronizedMap(new LinkedHashMap<>());
	private final GlobalContext globalContext;

	private OdinJector() {
		globalContext = new GlobalContext(contexts);
	}

	public static OdinJector create() {
		return new OdinJector();
	}

	public OdinJector addContext(Context context) {
		context.init();
		contexts.put(context.getClass(), context);
		return this;
	}

	public OdinJector addDynamicContext(Context context) {
		context.init();
		dynamicContexts.put(context.getMarkedContext(), context);
		return this;
	}

	public <T> T getInstance(Class<T> type) {
		return getInstance(InjectionContext.get(new ArrayList<>(), type));
	}

	public <T> Optional<T> getOptionalInstance(Class<T> type) {
		return Optional.ofNullable(getInstance(InjectionContext.get(new ArrayList<>(), type, InjectionOptions.get().optional())));
	}

	public <T> T getInstance(Class<?> context, Class<T> type) {
		return getInstance(InjectionContext.get(new ArrayList<>(getDynamicContexts(Collections.singletonList(context))), type));
	}

	public <T> T getOptionalInstance(Class<?> context, Class<T> type) {
		return getInstance(InjectionContext.get(new ArrayList<>(getDynamicContexts(Collections.singletonList(context))), type, InjectionOptions.get().optional()));
	}

	public <T> List<T> getInstances(Class<T> type) {
		return getInstances(InjectionContext.get(new ArrayList<>(), type));
	}

	public <T> List<T> getInstances(Class<?> context, Class<T> type) {
		return getInstances(InjectionContext.get(new ArrayList<>(getDynamicContexts(Collections.singletonList(context))), type));
	}

	@SuppressWarnings("unchecked")
	<T> T getInstance(InjectionContext<T> injectionContext) {
		return (T)providers.computeIfAbsent(injectionContext.getCurrentKey(), c -> {
			setup(injectionContext);

			BindingResult<T> binding = getBoundClass(globalContext, injectionContext);
			if (binding.binding.getElementClass() == injectionContext.clazz
					&& (injectionContext.clazz.isInterface() || Modifier.isAbstract(injectionContext.clazz.getModifiers()))
			) {
				if (injectionContext.isOptional()) {
					return () -> null;
				} else {
					throw new InjectionException("Unable to find binding for: "+injectionContext.logOutput());
				}
			}

			setupForBinding(injectionContext, binding);

			Provider<T> provider = binding.binding.getProvider(globalContext, injectionContext, this);

			if (binding.binding.isSingleton()) {
				return () -> binding.context.singleton(injectionContext.clazz, provider);
			} else {
				return provider;
			}
		}).get();
	}

	@SuppressWarnings("unchecked")
	<T> List<T> getInstances(InjectionContext<T> injectionContext) {
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
			List<Class<?>> annotationContextClasses = Arrays.asList(annotation.value());
			Collection<? extends Context> annotationContexts = getDynamicContexts(annotationContextClasses);
			injectionContext.context.addAll(0, annotationContexts);
			injectionContext.addToNext(annotationContexts, annotation.recursive());
		}
	}

	private Collection<? extends Context> getDynamicContexts(List<Class<?>> annotationContexts) {
		return annotationContexts.stream().map(ac -> {
			if (!dynamicContexts.containsKey(ac)) {
				throw new InjectionException("Unable to find a registered dynamic context for: "+ac.getName());
			}
			return dynamicContexts.get(ac);
		}).collect(Collectors.toList());
	}

	private <T> void setup(InjectionContext<T> injectionContext) {
		Class<T> clazz = injectionContext.clazz;

		if (clazz.isAnnotationPresent(ContextualInject.class)) {
			List<Class<?>> contextClasses = Arrays.asList(clazz.getAnnotation(ContextualInject.class).value());

			injectionContext.context.addAll(0, getDynamicContexts(contextClasses));

			injectionContext.addNext(getDynamicContexts(contextClasses), clazz.getAnnotation(ContextualInject.class).recursive());
		}
	}


	private <T> BindingResult<T> getBoundClass(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBinding(thisInjectionContext);
	}

	private <T> List<BindingResult<T>> getBoundClasses(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBindings(thisInjectionContext);
	}
}
