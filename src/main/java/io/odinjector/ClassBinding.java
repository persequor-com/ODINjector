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
		for(int i=0;i<constructor.getParameters().length;i++) {
			args.add(getInjection(thisInjectionContext, injector, constructor.getParameters()[i].getType(), constructor.getParameters()[i].getAnnotatedType(), new BindingTarget.ParameterTarget(constructor.getParameters()[i])));
		}
		List<Consumer<T>> additionalInjectors = new ArrayList<>();
		for(Method method : toClass.getDeclaredMethods()) {
			if (method.isAnnotationPresent(Inject.class)) {
				List<Provider> methodArgs = new ArrayList<>();
				for(int x=0;x<method.getParameters().length;x++) {
					methodArgs.add(getInjection(thisInjectionContext, injector, method.getParameters()[x].getType(), method.getParameters()[x].getAnnotatedType(), new BindingTarget.ParameterTarget(method.getParameters()[x])));
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

		for(Field field : toClass.getDeclaredFields()) {
			if (field.isAnnotationPresent(Inject.class)) {
				field.setAccessible(true);
				additionalInjectors.add((t) -> {
					try {
						field.set(t, getInjection(thisInjectionContext, injector, field.getType(), field.getAnnotatedType(), new BindingTarget.FieldTarget(field)).get());
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				});
			}
		}

		return new ClassBindingProvider(thisInjectionContext, constructor, args, toClass, additionalInjectors);
	}

	private Provider<T> getInjection(InjectionContext<T> thisInjectionContext, OdinJector injector, Class type, AnnotatedType annotatedType, BindingTarget target) {
		return getInjectionProvider(thisInjectionContext, injector, type, annotatedType, target);
	}

	private Provider getInjectionProvider(InjectionContext<T> thisInjectionContext, OdinJector injector, Class type, AnnotatedType annotatedType, BindingTarget target) {
		if (type == List.class) {
			Class<?> listElementType = getClassFromType(annotatedType.getType());
			return () -> injector.getInstances(thisInjectionContext.nextContextFor(listElementType, target));
		} else if (type == Provider.class) {
			Class<?> providerElementType = getClassFromType(annotatedType.getType());
			return () -> (Provider)() -> injector.getInstance(thisInjectionContext.nextContextFor(providerElementType, target));
		} else {
			return () -> injector.getInstance(thisInjectionContext.nextContextFor(type, target));
		}
	}

	private static class ClassBindingProvider<C> implements Provider<C> {
		private InjectionContext<C> thisInjectionContext;
		private Constructor<?> constructor;
		private List<Provider> args;
		private Class<C> toClass;
		private List<Consumer<C>> additionalInjectors;

		private ClassBindingProvider(InjectionContext<C> thisInjectionContext, Constructor<?> constructor, List<Provider> args, Class<C> toClass, List<Consumer<C>> additionalInjectors) {
			this.thisInjectionContext = thisInjectionContext;
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
