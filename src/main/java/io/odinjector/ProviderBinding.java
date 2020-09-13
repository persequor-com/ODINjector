package io.odinjector;

import javax.inject.Provider;

public class ProviderBinding<T> implements Binding<T> {
	private Provider<T> provider;
	private ProviderBinding(Provider<T> provider) {
		this.provider = provider;
	}

	public static <C> ProviderBinding<C> of(Provider<C> provider) {
		return new ProviderBinding<C>(provider);
	}

	@Override
	public Provider<T> getProvider(Context context, InjectionContext<T> thisInjectionContext, InjectionContext<T> outsideInjectionContext, OdinJector injector) {
		return provider;
	}

	@Override
	public Class<T> getElementClass() {
		return null;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}
}
