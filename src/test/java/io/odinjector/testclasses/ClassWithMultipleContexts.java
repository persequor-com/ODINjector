package io.odinjector.testclasses;

import io.odinjector.ContextualInject;

import javax.inject.Inject;

@ContextualInject({MyAltCtx.class, MyOtherAltCtx.class})
public class ClassWithMultipleContexts {
	private Hierarchy hierarchy;
	private TestInterface1 testInterface1;

	@Inject
	public ClassWithMultipleContexts(Hierarchy hierarchy, TestInterface1 testInterface1) {
		this.hierarchy = hierarchy;
		this.testInterface1 = testInterface1;
	}

	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	public TestInterface1 getTestInterface1() {
		return testInterface1;
	}
}
