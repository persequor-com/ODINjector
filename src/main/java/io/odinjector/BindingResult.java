package io.odinjector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BindingResult<T> {
	Binding<T> binding = null;
	Context context = null;

	private BindingResult() {

	}

	public boolean isEmpty() {
		return binding == null;
	}

	public static <C> BindingResult<C> of(Binding<C> binding, Context context) {
		BindingResult result = new BindingResult();
		result.binding = binding;
		result.context = context;
		return result;
	}

	public static <T> BindingResult<T> empty() {
		return new BindingResult<>();
	}

	public boolean isInterface() {
		return binding.isInterface();
	}
}
