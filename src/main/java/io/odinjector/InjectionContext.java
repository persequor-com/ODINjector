package io.odinjector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class InjectionContext<T> {
	List<Context> context;
	Deque<List<Context>> nextContexts;
	List<Context> recursiveContext = new ArrayList<>();
	Class<T> clazz;

	static <C> InjectionContext<C> get(List<Context> context, Class<C> clazz) {
		InjectionContext<C> ic = new  InjectionContext<>();
		ic.context = context;
		ic.nextContexts = new ArrayDeque<>();
		ic.clazz = clazz;
		return ic;
	}

	public CurrentContext<T> getCurrentKey() {
		return new CurrentContext<>(context, clazz);
	}

	public <C> InjectionContext<C> nextContextFor(Class<C> parameterType) {
		ArrayDeque<List<Context>> nextContexts = new ArrayDeque<>(this.nextContexts);
		List<Context> nextContext = nextContexts.poll();
		if (nextContext == null) {
			nextContext = new ArrayList<>();
		}
		nextContext.addAll(recursiveContext);
		InjectionContext<C> ic = new  InjectionContext<>();
		ic.context = nextContext;
		ic.nextContexts = nextContexts;
		ic.clazz = parameterType;
		ic.recursiveContext = this.recursiveContext;
		return ic;
	}

	public void addNext(Collection<? extends Context> contexts, boolean recursive) {
		List<Context> nextContext = new ArrayList<>(this.context);
		nextContext.addAll(contexts);
		if (recursive) {
			recursiveContext.addAll(nextContext);
		} else {
			nextContexts.add(nextContext);
		}
	}

	public void addToNext(Collection<? extends Context> contexts, boolean recursive) {
		if (recursive) {
			recursiveContext.addAll(contexts);
		} else {
			if (nextContexts != null && !nextContexts.isEmpty()) {
				nextContexts.getFirst().addAll(contexts);
			} else {
				nextContexts = new ArrayDeque<>();
				nextContexts.add(new ArrayList<>(contexts));
			}
		}
	}

	public String logOutput() {
		return "class: "+clazz.getName()
				+" Context: "+(this.context != null ? this.context.stream().map(c -> c.getClass().getName()).collect(Collectors.joining(", ")):"")
				+" next: "+(this.nextContexts != null ? this.nextContexts.stream().map(l -> l.stream().map(cl -> cl.getClass().getName()).collect(Collectors.joining(","))).collect(Collectors.joining(" | ")):"");
	}

	public InjectionContext<T> copy() {
		InjectionContext<T> injectionContext = new InjectionContext<>();
		injectionContext.nextContexts = new ArrayDeque<>(this.nextContexts);
		injectionContext.recursiveContext = new ArrayList<>(this.recursiveContext);
		injectionContext.context = new ArrayList<>(this.context);
		injectionContext.clazz = this.clazz;
		return injectionContext;
	}

	public static class CurrentContext<T> {
		List<Context> context;
		Class<T> clazz;

		public CurrentContext(List<Context> context, Class<T> clazz) {
			this.context = context;
			this.clazz = clazz;
		}
	}
}
