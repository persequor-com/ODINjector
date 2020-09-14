package io.odinjector;

import javax.inject.Provider;

public class ProviderBinding<T> implements Binding<T> {
	private Provider<T> provider;
	private boolean setAsSingleton;

	private ProviderBinding(Provider<T> provider, boolean setAsSingleton) {
		this.provider = provider;
		this.setAsSingleton = setAsSingleton;
	}

	public static <C> ProviderBinding<C> of(Provider<C> provider) {
		return new ProviderBinding<>(provider, false);
	}

	public static <C> ProviderBinding<C> of(Provider<C> provider, boolean setAsSingleton) {
		return new ProviderBinding<>(provider, setAsSingleton);
	}

	@Override
	public Provider<T> getProvider(Context context, InjectionContext<T> thisInjectionContext, OdinJector injector) {
		return provider;
	}

	@Override
	public Class<T> getElementClass() {
		return null;
	}

	@Override
	public boolean isSingleton() {
		return setAsSingleton;
	}
}
