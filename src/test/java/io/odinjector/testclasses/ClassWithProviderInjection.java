package io.odinjector.testclasses;

import javax.inject.Inject;
import javax.inject.Provider;

public class ClassWithProviderInjection {
	private Provider<TestInterface1> testInterface1;

	@Inject
	public ClassWithProviderInjection(Provider<TestInterface1> testInterface1) {
		this.testInterface1 = testInterface1;
	}

	public TestInterface1 get() {
		return testInterface1.get();
	}
}
