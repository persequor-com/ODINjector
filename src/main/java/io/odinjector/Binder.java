package io.odinjector;

public interface Binder {
	<T> BindingTo<T> bind(Class<T> tClass);
}
