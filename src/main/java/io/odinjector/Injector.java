package io.odinjector;

import java.util.List;
import java.util.Optional;

public interface Injector {
	<T> T getInstance(Class<T> type);
	<T> T getInstance(Class<?> context, Class<T> type);
	<T> Optional<T> getOptionalInstance(Class<?> context, Class<T> type);
	<T> List<T> getInstances(Class<T> type);
	<T> List<T> getInstances(Class<?> context, Class<T> type);
}
