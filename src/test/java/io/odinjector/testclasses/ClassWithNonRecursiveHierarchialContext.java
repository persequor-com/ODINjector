package io.odinjector.testclasses;

import io.odinjector.ContextualInject;

import javax.inject.Inject;

@ContextualInject(value = MyAltCtx.class, recursive = false)
public class ClassWithNonRecursiveHierarchialContext {
	private Hierarchy hierarchy;

	@Inject
	public ClassWithNonRecursiveHierarchialContext(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	public Hierarchy getHierarchy() {
		return hierarchy;
	}
}
