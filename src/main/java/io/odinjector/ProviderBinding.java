package io.odinjector;

import javax.inject.Provider;

public class ProviderBinding<T> implements Binding<T> {
	private Provider<T> provider;
	private boolean setAsSingleton;
	private Class<T> clazz;

	private ProviderBinding(Provider<T> provider, boolean setAsSingleton, Class<T> clazz) {
		this.provider = provider;
		this.setAsSingleton = setAsSingleton;
		this.clazz = clazz;
	}

	public static <C> ProviderBinding<C> of(Provider<C> provider, Class<C> clazz) {
		return new ProviderBinding<>(provider, false, clazz);
	}

	public static <C> ProviderBinding<C> of(Provider<C> provider, Class<C> clazz, boolean setAsSingleton) {
		return new ProviderBinding<>(provider, setAsSingleton, clazz);
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

	@Override
	public boolean isInterface() {
		return false;
	}
}
