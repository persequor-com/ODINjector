package io.odinjector;

import javax.inject.Provider;
import java.util.List;

public interface Binding<T> {
	Provider<T> getProvider(Context context, InjectionContext<T> thisInjectionContext, OdinJector injector);
	Class<T> getElementClass();
	boolean isSingleton();
}
