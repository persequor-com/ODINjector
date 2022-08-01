package io.odinjector.testclasses;

import javax.inject.Inject;
import javax.inject.Provider;

public class LoopingProviderDependency2 {
    private Provider<LoopingProviderDependency1> dep1;

    @Inject
    public LoopingProviderDependency2(Provider<LoopingProviderDependency1> dep1) {
        this.dep1 = dep1;
    }
    public LoopingProviderDependency1 getDep() {
        return dep1.get();
    }

}
