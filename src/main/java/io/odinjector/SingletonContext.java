package io.odinjector;

import javax.inject.Provider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SingletonContext extends Context {
    Map<Class, Object> singletons = new ConcurrentHashMap<>();

    public <T> T singleton(Class clazz, Provider<T> provider) {
        return (T)singletons.computeIfAbsent(clazz, c2 -> provider.get());
    }
}
