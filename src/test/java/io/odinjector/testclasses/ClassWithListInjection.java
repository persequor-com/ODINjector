package io.odinjector.testclasses;

import javax.inject.Inject;
import java.util.List;

public class ClassWithListInjection {
	private List<TestInterface1> testInterface1;

	@Inject
	public ClassWithListInjection(List<TestInterface1> testInterface1) {
		this.testInterface1 = testInterface1;
	}

	public List<TestInterface1> get() {
		return testInterface1;
	}
}
