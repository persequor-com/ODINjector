package io.odinjector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public interface BindingTarget {
    Class<?> getTargetclass();
    Optional<Constructor<?>> getConstructor();
    Optional<Method> getMethod();
    Optional<Field> getField();
    default boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return getAnnotation(annotation) != null;
    }
    Annotation getAnnotation(Class<? extends Annotation> annotation);

    class FieldTarget implements BindingTarget {
        private Field field;

        public FieldTarget(Field field) {
            this.field = field;
        }

        @Override
        public Class<?> getTargetclass() {
            return field.getDeclaringClass();
        }

        @Override
        public Optional<Constructor<?>> getConstructor() {
            return Optional.empty();
        }

        @Override
        public Optional<Method> getMethod() {
            return Optional.empty();
        }

        @Override
        public Optional<Field> getField() {
            return Optional.of(field);
        }

        @Override
        public Annotation getAnnotation(Class<? extends Annotation> annotation) {
            return field.getAnnotation(annotation);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FieldTarget that = (FieldTarget) o;
            return Objects.equals(field, that.field);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field);
        }
    }

    class ParameterTarget implements BindingTarget {
        private Parameter target;

        public ParameterTarget(Parameter target) {
            this.target = target;
        }

        @Override
        public Class<?> getTargetclass() {
            return target.getDeclaringExecutable().getDeclaringClass();
        }

        @Override
        public Optional<Constructor<?>> getConstructor() {
            return target.getDeclaringExecutable().getClass() == Constructor.class ? Optional.of((Constructor<?>) target.getDeclaringExecutable()) : Optional.empty();
        }

        @Override
        public Optional<Method> getMethod() {
            return target.getDeclaringExecutable().getClass() == Method.class ? Optional.of((Method) target.getDeclaringExecutable()) : Optional.empty();
        }

        @Override
        public Optional<Field> getField() {
            return Optional.empty();
        }

        @Override
        public Annotation getAnnotation(Class<? extends Annotation> annotation) {
            Annotation res = target.getAnnotation(annotation);
            if (res != null) {
                return res;
            }
            res = target.getDeclaringExecutable().getAnnotation(annotation);
            if (res != null) {
                return res;
            }
            res = target.getDeclaringExecutable().getDeclaringClass().getAnnotation(annotation);
            if (res != null) {
                return res;
            }
            return null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParameterTarget that = (ParameterTarget) o;
            return Objects.equals(target, that.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target);
        }
    }

    class ClassTarget implements BindingTarget {
        private Class<?> target;

        public ClassTarget(Class<?> target) {
            this.target = target;
        }

        @Override
        public Class<?> getTargetclass() {
            return target;
        }

        @Override
        public Optional<Constructor<?>> getConstructor() {
            return Optional.empty();
        }

        @Override
        public Optional<Method> getMethod() {
            return Optional.empty();
        }

        @Override
        public Optional<Field> getField() {
            return Optional.empty();
        }

        @Override
        public Annotation getAnnotation(Class<? extends Annotation> annotation) {
            return target.getAnnotation(annotation);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassTarget that = (ClassTarget) o;
            return Objects.equals(target, that.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target);
        }
    }

    class UnBoundTarget implements BindingTarget {
        private Class<?> target;

        public UnBoundTarget(Class<?> target) {
            this.target = target;
        }
        @Override
        public Class<?> getTargetclass() {
            return target;
        }

        @Override
        public Optional<Constructor<?>> getConstructor() {
            return Optional.empty();
        }

        @Override
        public Optional<Method> getMethod() {
            return Optional.empty();
        }

        @Override
        public Optional<Field> getField() {
            return Optional.empty();
        }

        @Override
        public Annotation getAnnotation(Class<? extends Annotation> annotation) {
            return target.getAnnotation(annotation);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UnBoundTarget that = (UnBoundTarget) o;
            return Objects.equals(target, that.target);
        }

        @Override
        public int hashCode() {
            return Objects.hash(target);
        }
    }

}
