package io.odinjector.testclasses;

import io.odinjector.Binder;
import io.odinjector.Context;
import io.odinjector.ContextMarker;

public class MyAltCtxWithMarker extends Context {
	@Override
	public void configure(Binder binder) {
		binder.bind(Hierarchy.class).to(AltHierarchyImpl.class);
		binder.bind(TestInterface1.class).to(TestImpl2.class);
	}

	@Override
	public Class<? extends ContextMarker> getMarkedContext() {
		return MyAltCtxMarker.class;
	}
}
