package io.odinjector.testclasses;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RecursiveInstantiationChild {
    private TestImpl1 testImpl1;

    @Inject
    public RecursiveInstantiationChild(TestImpl1 testImpl1) {
        this.testImpl1 = testImpl1;
    }
}
