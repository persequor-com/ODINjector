package io.odinjector;

import io.odinjector.testclasses.MyClass;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;

import static org.junit.Assert.assertEquals;

public class TypeHelperTest {
	private Constructor<?> constructor;
	private TypeHelper helper;

	@Before
	public void setup() {
		constructor = MyClass.class.getConstructors()[0];
		helper = new TypeHelper();
	}

	@Test
	public void getListParameterizedType() {
		AnnotatedType t = constructor.getParameters()[0].getAnnotatedType();
		int a = 0;
//		Class actual = helper.get();

//		assertEquals(TestInterface1.class, actual);
	}
}