package io.odinjector.testclasses;

import javax.inject.Inject;

public class RecursiveInstantiationParent {
    private TestImpl1 testImpl1;
    private RecursiveInstantiationChild child;

    @Inject
    public RecursiveInstantiationParent(RecursiveInstantiationChild child, TestImpl1 testImpl1) {
        this.testImpl1 = testImpl1;
        this.child = child;
    }

    public RecursiveInstantiationChild getChild() {
        return child;
    }
}
