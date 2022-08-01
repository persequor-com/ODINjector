package io.odinjector.testclasses;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RecursiveInstantiationSingletonParent {
    private RecursiveInstantiationParent parent;

    @Inject
    public RecursiveInstantiationSingletonParent(RecursiveInstantiationParent parent) {
        this.parent = parent;
    }

    public RecursiveInstantiationParent getParent() {
        return parent;
    }
}
