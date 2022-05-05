package io.odinjector;

import javax.inject.Provider;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ProviderBinding<T> implements Binding<T> {
	private Provider<T> provider;
	private Provides<? extends T> provides;
	private boolean setAsSingleton;
	private BindingKey<T> clazz;

	private ProviderBinding(Provider<T> provider, boolean setAsSingleton, BindingKey<T> clazz) {
		this.provider = provider;
		this.setAsSingleton = setAsSingleton;
		this.clazz = clazz;
	}

	private ProviderBinding(Provides<? extends T> provides, boolean setAsSingleton, BindingKey<T> clazz) {
		this.provides = provides;
		this.setAsSingleton = setAsSingleton;
		this.clazz = clazz;
	}

	public static <C> ProviderBinding<C> of(Provider<C> provider, BindingKey<C> clazz) {
		return new ProviderBinding<>(provider, false, clazz);
	}

	public static <C> ProviderBinding<C> of(Provider<C> provider, BindingKey<C> clazz, boolean setAsSingleton) {
		return new ProviderBinding<C>(provider, setAsSingleton, clazz);
	}

	public static <C> ProviderBinding<C> of(Provides<? extends C> provider, BindingKey<C> clazz, boolean setAsSingleton) {
		return new ProviderBinding<C>(provider, setAsSingleton, clazz);
	}

	@Override
	public Provider<T> getProvider(Context context, InjectionContext<T> thisInjectionContext, OdinJector injector) {
		if (provider != null) {
			return provider;
		}
		if (provides != null) {
			return () -> provides.call(injector);
		}
		throw new RuntimeException();
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

