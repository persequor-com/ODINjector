package io.odinjector.testclasses;

import io.odinjector.Binder;
import io.odinjector.Context;

public class MyCtx extends Context {
	@Override
	public void configure(Binder binder) {
		binder.bind(InterfaceForClassWithNonRecursiveHierarchialContext.class).to(ClassWithNonRecursiveHierarchialContext.class);
		binder.bind(Hierarchy.class).to(HierarchyImpl.class);
		binder.bind(TestInterface1.class).to(TestImpl1.class);
	}
}
