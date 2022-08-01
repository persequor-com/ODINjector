package io.odinjector.testclasses;

import javax.inject.Inject;
import javax.inject.Provider;

public class LoopingProviderDependency1 {
    private Provider<LoopingProviderDependency2> dep2;

    @Inject
    public LoopingProviderDependency1(Provider<LoopingProviderDependency2> dep2) {
        this.dep2 = dep2;
    }

    public LoopingProviderDependency2 getDep() {
        return dep2.get();
    }
}
