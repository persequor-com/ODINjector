package io.odinjector.testclasses;

import io.odinjector.ContextualInject;

import javax.inject.Inject;

@ContextualInject(MyAltCtxMarker.class)
public class ContextualDependenciesWithMarker {
	private TestInterface1 dependency;

	@Inject
	public ContextualDependenciesWithMarker(TestInterface1 dependency) {
		this.dependency = dependency;
	}

	public TestInterface1 getDependency() {
		return dependency;
	}
}
