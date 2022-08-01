package io.odinjector;

import javax.inject.Provider;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

class Providers {
	private final Map<InjectionContextImpl.CurrentContext, Provider<Provider>> providers = new ConcurrentHashMap<>();

	private final Yggdrasill yggdrasill;
	private OdinJector odin;
	private Function<Class<?>, Object> fallback = null;

	public Providers(Yggdrasill yggdrasill, OdinJector odin) {
		this.yggdrasill = yggdrasill;
		this.odin = odin;
	}

	@SuppressWarnings("unchecked")
	public <T> Provider<T> get(InjectionContext<T> injectionContext) {
		try {
			Provider<T> providerMuh = (Provider<T>) providers.computeIfAbsent(injectionContext.getCurrentKey(), c -> () -> {
				configureInjectionContextBeforeBinding(injectionContext);
				BindingResult<T> binding = getBoundClass(yggdrasill, injectionContext);

				if (binding.isEmpty() || binding.isInterface()) {
					if (injectionContext.getBindingKey().isInterface() && fallback != null) {
						return () -> fallback.apply(injectionContext.getBindingKey().getBoundClass());
					}
					if (injectionContext.isOptional()) {
						return () -> null;
					} else {
						throw new InjectionException("Unable to find binding for: " + injectionContext.logOutput());
					}
				}

				configureInjectionContextOnBinding(injectionContext, binding);


				Provider<T> provider = binding.binding.getProvider(yggdrasill, injectionContext, odin);

				if (binding.binding.isSingleton()) {
					return new WrappingProvider(injectionContext, new SingletonProvider(binding.context.singleton(injectionContext.getBindingKey(), provider)));
				} else {
					return new WrappingProvider(injectionContext, provider);
				}
			});
			return (Provider<T>) providerMuh.get();
		} catch (Exception e) {
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Provider<List<T>> getAll(InjectionContext<T> injectionContext) {
		return providers.computeIfAbsent(injectionContext.getCurrentKey(), c -> () -> {
			configureInjectionContextBeforeBinding(injectionContext);

			List<BindingResult<T>> bindings = getBoundClasses(yggdrasill, injectionContext);

			return () -> bindings.stream().map(binding -> {
				InjectionContext<T> newInjectionContext = injectionContext.copy();
				configureInjectionContextOnBinding(newInjectionContext, binding);
				return binding.binding.getProvider(yggdrasill, newInjectionContext, odin).get();
			}).collect(Collectors.toList());
		}).get();
	}


	private <T> void configureInjectionContextOnBinding(InjectionContext<T> injectionContext, BindingResult<T> binding) {
		if (binding.binding.getElementClass() != null) {
			ContextConfiguration config = yggdrasill.getAnnotationConfiguration(binding.binding.getElementClass());

			if (config.contexts.size() > 0) {
				Collection<? extends Context> annotationContexts = yggdrasill.getDynamicContexts(config.contexts);
				injectionContext.getContext().addAll(0, annotationContexts);
				injectionContext.addToNext(annotationContexts, config.recursive);
			}
		}
	}

	private <T> void configureInjectionContextBeforeBinding(InjectionContext<T> injectionContext) {
		BindingKey<T> clazz = injectionContext.getBindingKey();

		ContextConfiguration config = yggdrasill.getAnnotationConfiguration(clazz.getBoundClass());
		if (config.contexts.size() > 0) {
			List<Class<?>> contextClasses = config.contexts;

			injectionContext.getContext().addAll(0, yggdrasill.getDynamicContexts(contextClasses));

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

	private static class WrappingProvider<T> implements Provider<T> {
		private final InjectionContext<T> injectionContext;
		private final Provider<T> provider;

		public WrappingProvider(InjectionContext<T> injectionContext, Provider<T> provider) {
			this.injectionContext = injectionContext;
			this.provider = provider;
		}

		@Override
		public T get() {
			T t = provider.get();
			injectionContext.applyBindingResultListeners(t);
			return injectionContext.wrap(t);
		}
	}
}
