package io.odinjector;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InjectionContextImpl<T> implements InjectionContext<T> {
	List<Context> context;
	Deque<List<Context>> nextContexts;
	List<Context> recursiveContext = new ArrayList<>();
	BindingKey<T> clazz;
	InjectionOptions options = InjectionOptions.get();
	BindingTarget target;
	private List<Function<T, T>> wrappers = new ArrayList<>();
	private Map<Class<?>, BindingResultListener> bindingResultListeners = new ConcurrentHashMap<>();

	static <C> InjectionContext<C> get(List<Context> context, BindingKey<C> clazz, InjectionOptions options) {
		InjectionContextImpl<C> ic = new InjectionContextImpl<>();
		ic.context = context;
		ic.nextContexts = new ArrayDeque<>();
		ic.clazz = clazz;
		ic.options = options;
		ic.target = new BindingTarget.UnBoundTarget(clazz);
		return ic;
	}

	static <C> InjectionContext<C> get(List<Context> context, BindingKey<C> clazz) {
		InjectionContextImpl<C> ic = new InjectionContextImpl<>();
		ic.context = context;
		ic.nextContexts = new ArrayDeque<>();
		ic.clazz = clazz;
		ic.options = InjectionOptions.get();
		ic.target = new BindingTarget.UnBoundTarget(clazz);
		return ic;
	}

	public CurrentContext<T> getCurrentKey() {
		return new CurrentContext<T>(context, clazz, target);
	}

	@Override
	public BindingKey<T> getBindingKey() {
		return clazz;
	}

	@Override
	public List<Context> getContext() {
		return context;
	}

	public <C> InjectionContextImpl<C> nextContextFor(Class<C> parameterType, BindingTarget target) {
		ArrayDeque<List<Context>> nextContexts = new ArrayDeque<>(this.nextContexts);
		List<Context> nextContext = nextContexts.poll();
		if (nextContext == null) {
			nextContext = new ArrayList<>();
		}
		nextContext.addAll(recursiveContext);
		InjectionContextImpl<C> ic = new InjectionContextImpl<>();
		ic.context = nextContext;
		ic.nextContexts = nextContexts;
		ic.clazz = BindingKey.get(parameterType);
		ic.recursiveContext = this.recursiveContext;
		ic.options = this.options.forNext();
		ic.bindingResultListeners = bindingResultListeners;
		ic.target = target;
		return ic;
	}

	@Override
	public void wrap(Function<T, T> t) {
		this.wrappers.add(t);
	}

	@Override
	public T wrap(T res) {
		for (Function<T, T> w : wrappers) {
			res = w.apply(res);
		}
		return res;
	}

	@Override
	public void setResultListeners(Map<Class<?>, BindingResultListener> bindingResultListeners) {
		this.bindingResultListeners = bindingResultListeners;
	}

	@Override
	public void applyBindingResultListeners(T res) {
		for (BindingResultListener brl : bindingResultListeners.values()) {
			brl.listen(new ResultModifier(this, res));
		}
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
				+" Context: "+(this.context != null ? this.context.stream().map(c -> c.getClass().getName()).collect(Collectors.joining(", ")):"");
	}

	public InjectionContext<T> copy() {
		InjectionContextImpl<T> injectionContext = new InjectionContextImpl<>();
		injectionContext.nextContexts = new ArrayDeque<>(this.nextContexts);
		injectionContext.recursiveContext = new ArrayList<>(this.recursiveContext);
		injectionContext.context = new ArrayList<>(this.context);
		injectionContext.clazz = this.clazz;
		injectionContext.options = InjectionOptions.get();
		return injectionContext;
	}

	@Override
	public BindingTarget getTarget() {
		return target;
	}

	public boolean isOptional() {
		return options.optional;
	}

	public BindingTarget getTarget(Class<?> toClass) {
		return target != null ? target : new BindingTarget.ClassTarget(toClass);
	}

	public static class CurrentContext<T> {
		List<Context> context;
		BindingKey<T> bindingKey;
		BindingTarget bindingTarget;

		public CurrentContext(List<Context> context, BindingKey<T> bindingKey, BindingTarget bindingTarget) {
			this.context = context;
			this.bindingKey = bindingKey;
			this.bindingTarget = bindingTarget;
		}


		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			CurrentContext<?> that = (CurrentContext<?>) o;
			return Objects.equals(context, that.context) && Objects.equals(bindingKey, that.bindingKey) && Objects.equals(bindingTarget, that.bindingTarget);
		}

		@Override
		public int hashCode() {
			return Objects.hash(context, bindingKey, bindingTarget);
		}
	}

	private class ResultModifier<T> implements BindingResultModifier<T> {
		private InjectionContextImpl<T> injectionContext;
		private T res;

		public ResultModifier(InjectionContextImpl<T> injectionContext, T res) {
			this.injectionContext = injectionContext;
			this.res = res;
		}

		@Override
		public BindingKey<? extends T> getSource() {
			return injectionContext.clazz;
		}

		@Override
		public BindingKey getBound() {
			return BindingKey.get(res.getClass());
		}

		@Override
		public void wrap(Function<T, T> t) {
			injectionContext.wrap(t);
		}
	}
}
