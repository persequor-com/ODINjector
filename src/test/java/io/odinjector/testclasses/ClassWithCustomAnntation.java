package io.odinjector.testclasses;

import javax.inject.Inject;

@CustomAnnotation
public class ClassWithCustomAnntation {
	private TestInterface1 testInterface;

	@Inject
	public ClassWithCustomAnntation(TestInterface1 testInterface) {
		this.testInterface = testInterface;
	}

	public TestInterface1 getInterface() {
		return testInterface;
	}
}
