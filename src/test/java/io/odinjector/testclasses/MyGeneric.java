package io.odinjector.testclasses;

import javax.inject.Inject;

public class MyGeneric<T extends TestInterface1> {
    T inner;

    @Inject
    public MyGeneric(T t) {
        this.inner = t;
    }

    public void doThis() {
        inner.muh();
    }
}
