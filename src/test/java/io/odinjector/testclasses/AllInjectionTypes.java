package io.odinjector.testclasses;

import javax.inject.Inject;

public class AllInjectionTypes {
    private TestInterface1 injection1;
    private TestInterface1 injection2;
    @Inject
    private TestInterface1 injection3;

    private TestInterface1 wrapped1;
    private TestInterface1 wrapped2;
    @Inject
    @Wrapped
    private TestInterface1 wrapped3;

    @Inject
    public AllInjectionTypes(TestInterface1 injection1, @Wrapped TestInterface1 wrapped1) {
        this.injection1 = injection1;
        this.wrapped1 = wrapped1;
    }

    @Inject
    public void injectionMethod(TestInterface1 injection2){
        this.injection2 = injection2;
    }

    @Inject
    @Wrapped
    public void injectionMethod2(TestInterface1 wrapped2){
        this.wrapped2 = wrapped2;
    }

    public void runAll() {
        injection1.muh();
        injection2.muh();
        injection3.muh();
        wrapped1.muh();
        wrapped2.muh();
        wrapped3.muh();
    }
}
