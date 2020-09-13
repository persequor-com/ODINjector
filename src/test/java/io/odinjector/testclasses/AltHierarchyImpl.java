package io.odinjector.testclasses;

import javax.inject.Inject;

public class AltHierarchyImpl implements Hierarchy {
	private TestInterface1 i;
	@Inject
	public AltHierarchyImpl(TestInterface1 i) {
		this.i = i;
	}

	@Override
	public TestInterface1 getTestInterface() {
		return i;
	}
}
