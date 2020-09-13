package io.odinjector.testclasses;

import io.odinjector.ContextualInject;

import javax.inject.Inject;

public interface InterfaceForClassWithNonRecursiveHierarchialContext {
	Hierarchy getHierarchy();
}
