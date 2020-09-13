package io.odinjector.testclasses;

import io.odinjector.Binder;
import io.odinjector.Context;

import javax.inject.Provider;

public class ProviderCtx extends Context {

	@Override
	public void configure(Binder binder) {
		binder.bind(TestInterface1.class).to(new Provider<TestInterface1>() {
			@Override
			public TestInterface1 get() {
				return new TestImpl2();
			}
		});
	}
}
