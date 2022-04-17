package io.odinjector;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings({"rawtypes","unchecked"})
public class ClassBinding<T> implements Binding<T> {
	Class<?> toClass;
	boolean setAsSingleton;

	private ClassBinding(Class<? extends T> toClass, boolean setAsSingleton) {
		this.toClass = toClass;
		this.setAsSingleton = setAsSingleton;
	}

	public static <T> ClassBinding<T> of(Class<? extends T> toClass) {
		return new ClassBinding<>(toClass, false);
	}

	public static <T> ClassBinding<T> of(Class<? extends T> toClass, boolean setAsSingleton) {
		return new ClassBinding<>(toClass, setAsSingleton);
	}

	public Provider<T> getProvider(Context context, InjectionContext<T> thisInjectionContext, OdinJector injector) {
		Constructor<?> constructor = Arrays.stream(toClass.getConstructors()).filter(const1 -> const1.isAnnotationPresent(Inject.class)).findFirst()
				.orElseGet(() -> Arrays.stream(toClass.getConstructors()).filter(c2 -> c2.getParameterTypes().length == 0).findFirst()
						.orElseThrow(() -> new InjectionException("Unable to find constructor which has the @Inject annotation or is parameterless on: "+toClass.getName())));


		List<Provider> args = new ArrayList<>();
		int i = 0;
		for(Class<?> parameterType : constructor.getParameterTypes()) {
			args.add(getInjection(thisInjectionContext, injector, constructor.getParameters(), i, parameterType));
			i++;
		}
		List<Consumer<T>> additionalInjectors = new ArrayList<>();
		for(Method method : toClass.getMethods()) {
			if (method.isAnnotationPresent(Inject.class)) {
				List<Provider> methodArgs = new ArrayList<>();
				int x = 0;
				for(Class<?> parameterType : method.getParameterTypes()) {
					methodArgs.add(getInjection(thisInjectionContext, injector, method.getParameters(), x, parameterType));
					x++;
				}
				additionalInjectors.add((t) -> {
					try {
						method.invoke(t,methodArgs.stream().map(Provider::get).toArray());
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new RuntimeException(e);
					}
				});
			}
		}

		return new ClassBindingProvider(constructor, args, toClass, additionalInjectors);
	}

	private Provider getInjection(InjectionContext<T> thisInjectionContext, OdinJector injector, Parameter[] parameters, int paramNum, Class<?> parameterType) {
		if (parameterType == List.class) {
			AnnotatedType annotatedType = parameters[paramNum].getAnnotatedType();
			Class<?> listElementType = getClassFromType(annotatedType.getType());
			return () -> injector.getInstances(thisInjectionContext.nextContextFor(listElementType));
		} else if (parameterType == Provider.class) {
			AnnotatedType annotatedType = parameters[paramNum].getAnnotatedType();
			Class<?> providerElementType = getClassFromType(annotatedType.getType());
			return () -> (Provider)() -> injector.getInstance(thisInjectionContext.nextContextFor(providerElementType));
		} else {
			return () -> injector.getInstance(thisInjectionContext.nextContextFor(parameterType));
		}
	}

	private static class ClassBindingProvider<C> implements Provider<C> {
		private Constructor<?> constructor;
		private List<Provider> args;
		private Class<C> toClass;
		private List<Consumer<C>> additionalInjectors;

		private ClassBindingProvider(Constructor<?> constructor, List<Provider> args, Class<C> toClass, List<Consumer<C>> additionalInjectors) {
			this.constructor = constructor;
			this.args = args;
			this.toClass = toClass;
			this.additionalInjectors = additionalInjectors;
		}

		@Override
		public C get() {
			try {
				C res = (C) constructor.newInstance(args.stream().map(Provider::get).toArray());
				additionalInjectors.forEach(c -> c.accept(res));
				return res;
			} catch (Exception e) {
				throw new InjectionException("Unable to construct "+toClass.getName(),e);
			}
		}
	}

	@Override
	public Class<T> getElementClass() {
		return (Class<T>)toClass;
	}

	@Override
	public boolean isSingleton() {
		return setAsSingleton || toClass.isAnnotationPresent(Singleton.class);
	}

	@Override
	public boolean isInterface() {
		return toClass.isInterface();
	}

	private Class<?> getClassFromType(Type type) {
		if (type instanceof ParameterizedType) {
			return (Class<?>)((ParameterizedType) type).getActualTypeArguments()[0];
		}
		throw new InjectionException("Could not find type parameter for generic class: "+type.getTypeName());
	}
}
