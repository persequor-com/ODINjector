package io.odinjector.testclasses;

import javax.inject.Inject;
import javax.inject.Provider;

public class ProviderAsSecondParameter {
	private Provider<TestInterface1> testInterface1;

	@Inject
	public ProviderAsSecondParameter(TestInterface1 testInterface1, Provider<TestInterface1> testInterface1Provider) {
		this.testInterface1 = testInterface1Provider;
	}

	public TestInterface1 get() {
		return testInterface1.get();
	}
}
