package io.odinjector;

import javax.inject.Provider;

public class SingletonProvider implements Provider {
    private Object singleton;

    public <T> SingletonProvider(T singleton) {
        this.singleton = singleton;
    }

    @Override
    public Object get() {
        return singleton;
    }
}
