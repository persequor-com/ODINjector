package io.odinjector.testclasses;

import javax.inject.Inject;

public class ClassWithMethodInjection {
    private TestInterface1 testInterface1;

    @Inject
    public void injectHerePlease(TestInterface1 testInterface1) {
        this.testInterface1 = testInterface1;
    }

    public TestInterface1 get() {
        return testInterface1;
    }
}
