package io.odinjector;

import javax.inject.Provider;
import java.util.Collections;

public class ContextBinder implements Binder {
	private Context context;

	public ContextBinder(Context context) {
		this.context = context;
	}

	@Override
	public <T> BindingTo<T> bind(Class<T> fromClass) {
		return new BindingTo<T>() {
			@Override
			public void to(Class<? extends T> toClass) {
				context.contextBindings.put(fromClass, Collections.singletonList(ClassBinding.of(toClass)));
			}

			@Override
			public void to(Provider<? extends T> provider) {
				context.contextBindings.put(fromClass, Collections.singletonList(ProviderBinding.of(provider)));
			}
		};
	}
}
