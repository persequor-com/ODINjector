package io.odinjector.testclasses;

import io.odinjector.Binder;
import io.odinjector.Context;

public class SingletonCtx extends Context {
	@Override
	public void configure(Binder binder) {
		binder.bind(Hierarchy.class).asSingleton().to(HierarchyImpl.class);
		binder.bind(TestInterface1.class).to(SingletonImpl.class);
	}
}
