package io.odinjector;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ContextBinder implements Binder {
	private Context context;

	public ContextBinder(Context context) {
		this.context = context;
	}

	@Override
	public <T> BindingTo<T> bind(Class<T> fromClass) {
		return new BindingToImpl<T>(context, BindingKey.get(fromClass));
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

	@Override
	public <T> BindingTo<T> bind(BindingKey<T> bindingKey) {
		return new BindingToImpl<T>(context, bindingKey);
	}

	private static class BindingToImpl<T> implements BindingTo<T> {
		private final Context context;
		private boolean setAsSingleton = false;
		private BindingKey<T> fromClass;
		public BindingToImpl(Context context, BindingKey<T> fromClass) {
			this.context = context;
			this.fromClass = fromClass;
		}

		@Override
		public void to(Class<? extends T> toClass) {
			context.contextBindings.put(fromClass, Collections.singletonList(ClassBinding.of(BindingKey.get(toClass), setAsSingleton)));
		}

		@Override
		public void to(Provider<? extends T> provider) {
			context.contextBindings.put(fromClass, Collections.singletonList(ProviderBinding.of((Provider)provider, fromClass, setAsSingleton)));
		}


		@Override
		public void to(Provides<? extends T> provides) {
			context.contextBindings.put(fromClass, Collections.singletonList(ProviderBinding.of(provides, fromClass, setAsSingleton)));
		}

		@Override
		public BindingTo<T> asSingleton() {
			this.setAsSingleton = true;
			return this;
		}

		@Override
		public void add(Class<? extends T> toClass) {
			context.contextBindings.computeIfAbsent(fromClass, f -> new ArrayList<>()).add(ClassBinding.of(BindingKey.get(toClass), setAsSingleton));
		}

	}
}
