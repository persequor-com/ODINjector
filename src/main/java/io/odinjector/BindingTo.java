package io.odinjector;

import javax.inject.Provider;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public interface BindingTo<T> {
	void to(Class<? extends T> toClass);
	void to(Provider<? extends T> toProvider);
	void to(Provides<? extends T> o);
	BindingTo<T> asSingleton();
	void add(Class<? extends T> toClass);

}
