package io.odinjector;

import io.odinjector.testclasses.ClassWithInterfaceInjection;
import io.odinjector.testclasses.ClassWithListInjection;
import io.odinjector.testclasses.ClassWithProviderInjection;
import io.odinjector.testclasses.MyAltCtx;
import io.odinjector.testclasses.MyCtx;
import io.odinjector.testclasses.SingletonCtx;
import io.odinjector.testclasses.SingletonImpl;
import io.odinjector.testclasses.TestImpl1;
import io.odinjector.testclasses.TestImpl2;
import io.odinjector.testclasses.TestInterface1;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OdinJectorTest {
	OdinJector odinJector;

	@Before
	public void before() {
		odinJector = OdinJector.create().addContext(MyCtx.class);
	}

	@Test
	public void getInterfaceBinding() {
		TestInterface1 actual = odinJector.getInstance(TestInterface1.class);

		assertSame(TestImpl1.class, actual.getClass());
	}

	@Test
	public void getActualInstance() {
		TestImpl1 actual = odinJector.getInstance(TestImpl1.class);
		assertSame(TestImpl1.class, actual.getClass());
	}

	@Test
	public void getClassWithInterfaceBindingDependency() {
		ClassWithInterfaceInjection actual = odinJector.getInstance(ClassWithInterfaceInjection.class);

		assertSame(TestImpl1.class, actual.get().getClass());
	}

	@Test
	public void getClassWithProviderDependency() {
		ClassWithProviderInjection actual = odinJector.getInstance(ClassWithProviderInjection.class);

		assertSame(TestImpl1.class, actual.get().getClass());
	}

	@Test
	public void getClassWithListDependency() {
		ClassWithListInjection actual = odinJector.getInstance(ClassWithListInjection.class);

		assertEquals(1, actual.get().size());
		assertSame(TestImpl1.class, actual.get().get(0).getClass());
	}

	@Test
	public void getFromAlternateContext() {
		odinJector.addContext(MyAltCtx.class);
		TestInterface1 actual = odinJector.getInstance(TestInterface1.class);

		assertSame(TestImpl2.class, actual.getClass());
	}

	@Test
	public void getWithInjectionFromAlternateContext() {
		odinJector.addContext(MyAltCtx.class);
		ClassWithInterfaceInjection actual = odinJector.getInstance(ClassWithInterfaceInjection.class);

		assertSame(TestImpl2.class, actual.get().getClass());
	}

	@Test
	public void getNotSingleton() {
		TestImpl1 actual1 = odinJector.getInstance(TestImpl1.class);
		TestImpl2 actual2 = odinJector.getInstance(TestImpl2.class);

		assertNotSame(actual1, actual2);
	}

	@Test
	public void getSingleton() {
		SingletonImpl actual1 = odinJector.getInstance(SingletonImpl.class);
		SingletonImpl actual2 = odinJector.getInstance(SingletonImpl.class);

		assertSame(actual1, actual2);
	}

	@Test
	public void getNotSingleton_fromDependency() {
		ClassWithInterfaceInjection actual1 = odinJector.getInstance(ClassWithInterfaceInjection.class);
		ClassWithInterfaceInjection actual2 = odinJector.getInstance(ClassWithInterfaceInjection.class);

		assertNotSame(actual1.get(), actual2.get());
	}

	@Test
	public void getSingleton_fromDependency() {
		odinJector.addContext(SingletonCtx.class);
		TestInterface1 actual1 = odinJector.getInstance(ClassWithInterfaceInjection.class).get();
		TestInterface1 actual2 = odinJector.getInstance(ClassWithInterfaceInjection.class).get();

		assertSame(actual1, actual2);
	}
}