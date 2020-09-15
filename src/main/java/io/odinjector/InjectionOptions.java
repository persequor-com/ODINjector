package io.odinjector;

public class InjectionOptions {
	boolean optional = false;
	private InjectionOptions() {

	}

	public static InjectionOptions get() {
		return new InjectionOptions();
	}

	public InjectionOptions optional() {
		optional = true;
		return this;
	}

	public InjectionOptions forNext() {
		InjectionOptions options = new InjectionOptions();
		// Do not pass optional to next level
		return options;
	}
}
