package io.odinjector;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class Providers {
	private final Map<InjectionContext.CurrentContext, Provider> providers = Collections.synchronizedMap(new LinkedHashMap<>());

	private final Yggdrasill yggdrasill;
	private OdinJector odin;

	public Providers(Yggdrasill yggdrasill, OdinJector odin) {
		this.yggdrasill = yggdrasill;
		this.odin = odin;
	}

	@SuppressWarnings("unchecked")
	public <T> Provider<T> get(InjectionContext<T> injectionContext) {
		return (Provider<T>) providers.computeIfAbsent(injectionContext.getCurrentKey(), c -> {
			configureInjectionContextBeforeBinding(injectionContext);

			BindingResult<T> binding = getBoundClass(yggdrasill, injectionContext);
			if (binding.isEmpty()) {
				if (injectionContext.isOptional()) {
					return () -> null;
				} else {
					throw new InjectionException("Unable to find binding for: "+injectionContext.logOutput());
				}
			}

			configureInjectionContextOnBinding(injectionContext, binding);

			Provider<T> provider = binding.binding.getProvider(yggdrasill, injectionContext, odin);

			if (binding.binding.isSingleton()) {
				return () -> binding.context.singleton(injectionContext.clazz, provider);
			} else {
				return provider;
			}
		});
	}

	@SuppressWarnings("unchecked")
	public <T> Provider<List<T>> getAll(InjectionContext<T> injectionContext) {
		return providers.computeIfAbsent(injectionContext.getCurrentKey(), c -> {
			configureInjectionContextBeforeBinding(injectionContext);

			List<BindingResult<T>> bindings = getBoundClasses(yggdrasill, injectionContext);

			return () -> bindings.stream().map(binding -> {
				InjectionContext<T> newInjectionContext = injectionContext.copy();
				configureInjectionContextOnBinding(newInjectionContext, binding);
				return binding.binding.getProvider(yggdrasill, newInjectionContext, odin).get();
			}).collect(Collectors.toList());
		});
	}


	private <T> void configureInjectionContextOnBinding(InjectionContext<T> injectionContext, BindingResult<T> binding) {
		if (binding.binding.getElementClass() != null && binding.binding.getElementClass().isAnnotationPresent(ContextualInject.class)) {
			ContextualInject annotation = binding.binding.getElementClass().getAnnotation(ContextualInject.class);
			List<Class<?>> annotationContextClasses = Arrays.asList(annotation.value());
			Collection<? extends Context> annotationContexts = yggdrasill.getDynamicContexts(annotationContextClasses);
			injectionContext.context.addAll(0, annotationContexts);
			injectionContext.addToNext(annotationContexts, annotation.recursive());
		}
	}

	private <T> void configureInjectionContextBeforeBinding(InjectionContext<T> injectionContext) {
		Class<T> clazz = injectionContext.clazz;

		if (clazz.isAnnotationPresent(ContextualInject.class)) {
			ContextualInject annotation = clazz.getAnnotation(ContextualInject.class);
			List<Class<?>> contextClasses = Arrays.asList(annotation.value());

			injectionContext.context.addAll(0, yggdrasill.getDynamicContexts(contextClasses));

			injectionContext.addNext(yggdrasill.getDynamicContexts(contextClasses), annotation.recursive());
		}
	}




	private <T> BindingResult<T> getBoundClass(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBinding(thisInjectionContext);
	}

	private <T> List<BindingResult<T>> getBoundClasses(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBindings(thisInjectionContext);
	}
}
