package io.odinjector;

import javax.inject.Provider;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class Providers {
	private final Map<InjectionContext.CurrentContext, Provider> providers = Collections.synchronizedMap(new LinkedHashMap<>());

	private final Yggdrasill yggdrasill;
	private OdinJector odin;
	private Function<Class<?>, Object> fallback = null;

	public Providers(Yggdrasill yggdrasill, OdinJector odin) {
		this.yggdrasill = yggdrasill;
		this.odin = odin;
	}

	@SuppressWarnings("unchecked")
	public <T> Provider<T> get(InjectionContext<T> injectionContext) {
		return (Provider<T>) providers.computeIfAbsent(injectionContext.getCurrentKey(), c -> {
			configureInjectionContextBeforeBinding(injectionContext);

			BindingResult<T> binding = getBoundClass(yggdrasill, injectionContext);

			if (binding.isEmpty() || binding.isInterface()) {
				if (injectionContext.clazz.isInterface() && fallback != null) {
					return () -> fallback.apply(injectionContext.clazz);
				}
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
		if (binding.binding.getElementClass() != null) {
			ContextConfiguration config = yggdrasill.getAnnotationConfiguration(binding.binding.getElementClass());

			if (config.contexts.size() > 0) {
				Collection<? extends Context> annotationContexts = yggdrasill.getDynamicContexts(config.contexts);
				injectionContext.context.addAll(0, annotationContexts);
				injectionContext.addToNext(annotationContexts, config.recursive);
			}
		}
	}

	private <T> void configureInjectionContextBeforeBinding(InjectionContext<T> injectionContext) {
		Class<T> clazz = injectionContext.clazz;

		ContextConfiguration config = yggdrasill.getAnnotationConfiguration(clazz);
		if (config.contexts.size() > 0) {
			List<Class<?>> contextClasses = config.contexts;

			injectionContext.context.addAll(0, yggdrasill.getDynamicContexts(contextClasses));

			injectionContext.addNext(yggdrasill.getDynamicContexts(contextClasses), config.recursive);
		}
	}




	private <T> BindingResult<T> getBoundClass(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBinding(thisInjectionContext);
	}

	private <T> List<BindingResult<T>> getBoundClasses(Context context, InjectionContext<T> thisInjectionContext) {
		return context.getBindings(thisInjectionContext);
	}

	public void setFallback(Function<Class<?>, Object> fallback) {
		this.fallback = fallback;
	}
}
