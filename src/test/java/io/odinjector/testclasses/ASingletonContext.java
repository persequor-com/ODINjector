package io.odinjector.testclasses;

import io.odinjector.Binder;
import io.odinjector.SingletonContext;

public class ASingletonContext extends SingletonContext {
    @Override
    public void configure(Binder binder) {
        binder.bind(TestInterface1.class).to(SingletonImpl.class);
    }
}
