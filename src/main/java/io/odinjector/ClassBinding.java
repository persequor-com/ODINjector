package io.odinjector;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

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


		Object[] args = new Object[constructor.getParameterTypes().length];
		int i=0;
		for(Class<?> parameterType : constructor.getParameterTypes()) {
			if (parameterType == List.class) {
				AnnotatedType annotatedType = constructor.getParameters()[i].getAnnotatedType();
				Class<?> listElementType = getClassFromType(annotatedType.getType());
				args[i++] = injector.getInstances(thisInjectionContext.nextContextFor(listElementType));
//				args[i++] = context.getBindings(thisInjectionContext.context, listElementType).stream().map().collect(Collectors.toList());
			} else if (parameterType == Provider.class) {
				AnnotatedType annotatedType = constructor.getParameters()[i].getAnnotatedType();
				Class<?> providerElementType = getClassFromType(annotatedType.getType());
				args[i++] = (Provider)() -> injector.getInstance(thisInjectionContext.nextContextFor(providerElementType));
			} else {
				args[i++] = injector.getInstance(thisInjectionContext.nextContextFor(parameterType));
			}
		}

		return new ClassBindingProvider(constructor, args, toClass);
	}

	private static class ClassBindingProvider<C> implements Provider<C> {
		private Constructor<?> constructor;
		private Object[] args;
		private Class<C> toClass;

		private ClassBindingProvider(Constructor<?> constructor, Object[] args, Class<C> toClass) {
			this.constructor = constructor;
			this.args = args;
			this.toClass = toClass;
		}

		@Override
		public C get() {
			try {
				return (C)constructor.newInstance(args);
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

	private Class<?> getClassFromType(Type type) {
		if (type instanceof ParameterizedType) {
			return (Class<?>)((ParameterizedType) type).getActualTypeArguments()[0];
		}
		throw new InjectionException("Could not find element type for: "+type.getTypeName());
	}
}
