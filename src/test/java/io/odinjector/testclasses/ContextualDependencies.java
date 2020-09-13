package io.odinjector.testclasses;

import io.odinjector.ContextualInject;

import javax.inject.Inject;

@ContextualInject(MyAltCtx.class)
public class ContextualDependencies {
	private TestInterface1 dependency;

	@Inject
	public ContextualDependencies(TestInterface1 dependency) {
		this.dependency = dependency;
	}

	public TestInterface1 getDependency() {
		return dependency;
	}
}
