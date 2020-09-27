package io.odinjector.testclasses;

import javax.inject.Inject;

public class ExtendingClassWithCustomAnnotation extends ClassWithCustomAnntation {
	@Inject
	public ExtendingClassWithCustomAnnotation(TestInterface1 testInterface) {
		super(testInterface);
	}
}
