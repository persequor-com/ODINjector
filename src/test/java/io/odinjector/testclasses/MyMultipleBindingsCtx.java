package io.odinjector.testclasses;

import io.odinjector.Binder;
import io.odinjector.Context;

public class MyMultipleBindingsCtx extends Context {
	@Override
	public void configure(Binder binder) {
		binder.bind(TestInterface1.class).add(TestImpl1.class);
		binder.bind(TestInterface1.class).add(TestImpl2.class);
		binder.bind(TestInterface1.class).add(TestImpl3.class);
	}
}
