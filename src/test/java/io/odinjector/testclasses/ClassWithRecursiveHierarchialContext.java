package io.odinjector.testclasses;

import io.odinjector.ContextualInject;

import javax.inject.Inject;

@ContextualInject(value = MyAltCtx.class, recursive = true)
public class ClassWithRecursiveHierarchialContext {
	private Hierarchy hierarchy;

	@Inject
	public ClassWithRecursiveHierarchialContext(Hierarchy hierarchy) {
		this.hierarchy = hierarchy;
	}

	public Hierarchy getHierarchy() {
		return hierarchy;
	}
}
