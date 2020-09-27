package io.odinjector;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContextConfiguration {
	boolean recursive = false;
	List<Class<?>> contexts = new ArrayList<>();

	public ContextConfiguration recursive(boolean recursive) {
		this.recursive = recursive;
		return this;
	}

	public ContextConfiguration addContext(Class<?>... context) {
		contexts.addAll(Arrays.asList(context));
		return this;
	}
}
