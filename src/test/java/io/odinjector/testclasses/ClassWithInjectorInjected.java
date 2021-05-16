package io.odinjector.testclasses;

import io.odinjector.Injector;

import javax.inject.Inject;

public class ClassWithInjectorInjected {
	private Injector injector;

	@Inject
	public ClassWithInjectorInjected(Injector injector) {
		this.injector = injector;
	}

	public TestInterface1 getImplementation() {
		return injector.getInstance(TestImpl1.class);
	}
}
