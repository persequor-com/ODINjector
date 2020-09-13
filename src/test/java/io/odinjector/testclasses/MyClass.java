package io.odinjector.testclasses;

import javax.inject.Inject;
import java.util.List;

public class MyClass {
	private List<TestInterface1> injection;

	@Inject
	public MyClass(List<TestInterface1> injection) {
		this.injection = injection;
	}

	public void muh() {
		injection.forEach(TestInterface1::muh);
	}
}
