package io.odinjector.testclasses;

import javax.inject.Inject;

public class HierarchyImpl implements Hierarchy {
	private TestInterface1 i;
	@Inject
	public HierarchyImpl(TestInterface1 i) {
		this.i = i;
	}

	@Override
	public TestInterface1 getTestInterface() {
		return i;
	}
}
