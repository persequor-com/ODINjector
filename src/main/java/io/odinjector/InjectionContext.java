package io.odinjector;

import java.util.ArrayList;
import java.util.List;

public class InjectionContext<T> {
	List<Class<? extends Context>> context;
	Class<T> clazz;

	static <C> InjectionContext<C> get(List<Class<? extends Context>> context, Class<C> clazz) {
		InjectionContext<C> ic = new  InjectionContext<>();
		ic.context = context;
		ic.clazz = clazz;
		return ic;
	}

	public InjectionContext<T> copy() {
		return InjectionContext.get(new ArrayList<>(context), clazz);
	}

	public <C> InjectionContext<C> contextFor(Class<C> parameterType) {
		return InjectionContext.get(new ArrayList<>(context), parameterType);
	}
}
