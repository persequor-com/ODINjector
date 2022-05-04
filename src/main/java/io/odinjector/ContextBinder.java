package io.odinjector;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;

public class ContextBinder implements Binder {
	private Context context;

	public ContextBinder(Context context) {
		this.context = context;
	}

	@Override
	public <T> BindingTo<T> bind(Class<T> fromClass) {
		return new BindingTo<T>() {
			private boolean setAsSingleton = false;

			@Override
			public void to(Class<? extends T> toClass) {
				context.contextBindings.put(fromClass, Collections.singletonList(ClassBinding.of(toClass, setAsSingleton)));
			}

			@Override
			public void to(Provider<? extends T> provider) {
				context.contextBindings.put(fromClass, Collections.singletonList(ProviderBinding.of((Provider)provider, fromClass, setAsSingleton)));
			}

			@Override
			public BindingTo<T> asSingleton() {
				this.setAsSingleton = true;
				return this;
			}

			@Override
			public void add(Class<? extends T> toClass) {
				context.contextBindings.computeIfAbsent(fromClass, f -> new ArrayList<>()).add(ClassBinding.of(toClass, setAsSingleton));
			}
		};
	}

	@Override
	public void injectionListener(BindingListener listener) {
		context.addListener(listener);
	}

	@Override
	public void bindPackageToContext(Package aPackage) {
		context.addPackageBinding(aPackage);
	}

	@Override
	public void bindingResultListener(BindingResultListener listener) {
		context.addBindingResultListener(listener);
	}
}
