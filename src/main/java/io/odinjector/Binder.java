package io.odinjector;

public interface Binder {
	<T> BindingTo<T> bind(Class<T> tClass);
	void injectionListener(BindingListener listener);
	void bindPackageToContext(Package aPackage);
	void bindingResultListener(BindingResultListener listener);
}
