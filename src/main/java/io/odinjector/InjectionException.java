package io.odinjector;

public class InjectionException extends RuntimeException {
	public InjectionException(String s) {
		super(s);
	}
	public InjectionException(String s, Exception e) {
		super(s, e);
	}
	public InjectionException(Exception e) {
		super(e);
	}
}
