package io.odinjector;

import javax.inject.Provider;

public interface BindingTo<T> {
	void to(Class<? extends T> toClass);
	void to(Provider<? extends T> toProvider);
	BindingTo<T> asSingleton();
	void add(Class<? extends T> toClass);
}
