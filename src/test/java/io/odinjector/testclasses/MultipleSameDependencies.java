package io.odinjector.testclasses;

import javax.inject.Inject;

public class MultipleSameDependencies {
    private final TestInterface1 in1;
    private final TestInterface1 in2;
    private final TestInterface1 in3;

    @Inject
    public MultipleSameDependencies(TestInterface1 in1, TestInterface1 in2, TestInterface1 in3) {
        this.in1 = in1;
        this.in2 = in2;
        this.in3 = in3;
    }

    public void run() {
        in1.muh();
        in2.muh();
        in3.muh();
    }
}
