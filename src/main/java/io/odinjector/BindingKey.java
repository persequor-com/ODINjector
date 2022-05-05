package io.odinjector;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class BindingKey<T> {
    Class<T> boundClass;
    List<Class<?>> generics = new ArrayList<>();

    public static <C> BindingKey<C> get(Class<C> boundClass, Class<?>... generics) {
        return get(boundClass, Arrays.asList(generics));
    }

    public static <C> BindingKey<C> get(Class<C> boundClass, List<Class<?>> generics) {
        return new BindingKey<C>(boundClass, generics);
   }

    public BindingKey(Class boundClass, List<Class<?>> generics) {
        this.boundClass = boundClass;
        this.generics = generics;
    }

    public BindingKey(Class boundClass, Class<?>... generics) {
        this(boundClass, Arrays.asList(generics));
    }

    public Class<T> getBoundClass() {
        return boundClass;
    }

    public List<Class<?>> getGenerics() {
        return generics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BindingKey<?> that = (BindingKey<?>) o;
        return boundClass.equals(that.boundClass) && generics.equals(that.generics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boundClass, generics);
    }

    public String getName() {
        return boundClass.getName();
    }

    public Method[] getDeclaredMethods() {
        return boundClass.getDeclaredMethods();
    }

    public boolean isInterface() {
        return boundClass.isInterface();
    }

    public int getModifiers() {
        return boundClass.getModifiers();
    }

    public Field[] getDeclaredFields() {
        return boundClass.getDeclaredFields();
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
        return boundClass.isAnnotationPresent(annotation);
    }
}
