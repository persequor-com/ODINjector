package io.odinjector.testclasses;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, CONSTRUCTOR, FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface Wrapped {
}
