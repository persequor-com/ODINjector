package io.odinjector;

import io.odinjector.testclasses.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OdinJectorTest {
	OdinJector odinJector;

	@Before
	public void before() {
		odinJector = OdinJector.create().addContext(new MyCtx()).addDynamicContext(new MyAltCtx()).addDynamicContext(new MyOtherAltCtx());
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
	public void getClassUsingMethodInjection() {
		ClassWithMethodInjection actual = odinJector.getInstance(ClassWithMethodInjection.class);

		assertSame(TestImpl1.class, actual.get().getClass());
	}

	@Test
	public void getClassWithProviderDependency() {
		ClassWithProviderInjection actual = odinJector.getInstance(ClassWithProviderInjection.class);

		assertSame(TestImpl1.class, actual.get().getClass());
	}

	@Test
	public void getClassWithProviderDependency_asSecondParameter() {
		ProviderAsSecondParameter actual = odinJector.getInstance(ProviderAsSecondParameter.class);

		assertSame(TestImpl1.class, actual.get().getClass());
	}

	@Test
	public void getClassWithListDependency() {
		ClassWithListInjection actual = odinJector.getInstance(ClassWithListInjection.class);

		assertEquals(1, actual.get().size());
		assertSame(TestImpl1.class, actual.get().get(0).getClass());
	}

	@Test
	public void getClassWithMultipleElementsInListDependency() {
		odinJector.addContext(new MyMultipleBindingsCtx());
		ClassWithListInjection actual = odinJector.getInstance(ClassWithListInjection.class);

		assertEquals(3, actual.get().size());
		assertSame(TestImpl1.class, actual.get().get(0).getClass());
		assertSame(TestImpl2.class, actual.get().get(1).getClass());
		assertSame(TestImpl3.class, actual.get().get(2).getClass());
	}

	@Test
	public void getFromAlternateContext() {
		odinJector.addContext(new MyAltCtx());
		TestInterface1 actual = odinJector.getInstance(TestInterface1.class);

		assertSame(TestImpl2.class, actual.getClass());
	}

	@Test
	public void getWithInjectionFromAlternateContext() {
		odinJector.addContext(new MyAltCtx());
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
	public void getSingleton_setInContext() {
		odinJector.addContext(new SingletonCtx());
		Hierarchy actual1 = odinJector.getInstance(Hierarchy.class);
		Hierarchy actual2 = odinJector.getInstance(Hierarchy.class);

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
		odinJector.addContext(new SingletonCtx());
		TestInterface1 actual1 = odinJector.getInstance(ClassWithInterfaceInjection.class).get();
		TestInterface1 actual2 = odinJector.getInstance(ClassWithInterfaceInjection.class).get();

		assertSame(actual1, actual2);
	}

	@Test
	public void getDependency_fromContextualInject() {
		ContextualDependencies actual1 = odinJector.getInstance(ContextualDependencies.class);

		assertSame(TestImpl2.class, actual1.getDependency().getClass());
	}

	@Test
	public void getDependency_fromContextualMarkerInject() {
		odinJector.addDynamicContext(new MyAltCtxWithMarker());
		ContextualDependenciesWithMarker actual1 = odinJector.getInstance(ContextualDependenciesWithMarker.class);

		assertSame(TestImpl2.class, actual1.getDependency().getClass());
	}

	@Test
	public void getDependency_fromMarkerInject() {
		odinJector.addContext(new MyAltCtxWithMarker());
		ContextualDependencies actual1 = odinJector.getInstance(ContextualDependencies.class);

		assertSame(TestImpl2.class, actual1.getDependency().getClass());
	}

	@Test
	public void getDependency_fromNonRecurisveContextualInject() {
		ClassWithNonRecursiveHierarchialContext actual = odinJector.getInstance(ClassWithNonRecursiveHierarchialContext.class);

		assertSame(AltHierarchyImpl.class, actual.getHierarchy().getClass());
		assertSame(TestImpl1.class, actual.getHierarchy().getTestInterface().getClass());
	}

	@Test
	public void getDependency_fromRecurisveContextualInject() {
		ClassWithRecursiveHierarchialContext actual = odinJector.getInstance(ClassWithRecursiveHierarchialContext.class);

		assertSame(AltHierarchyImpl.class, actual.getHierarchy().getClass());
		assertSame(TestImpl2.class, actual.getHierarchy().getTestInterface().getClass());
	}

	@Test
	public void getDependency_fromNonRecurisveContextualInject_setOnResolvedClass() {
		InterfaceForClassWithNonRecursiveHierarchialContext actual = odinJector.getInstance(InterfaceForClassWithNonRecursiveHierarchialContext.class);

		assertSame(AltHierarchyImpl.class, actual.getHierarchy().getClass());
		assertSame(TestImpl1.class, actual.getHierarchy().getTestInterface().getClass());
	}

	@Test
	public void getDependency_withMultipleContextualClasses() {
		ClassWithMultipleContexts actual = odinJector.getInstance(ClassWithMultipleContexts.class);

		assertSame(AltHierarchyImpl.class, actual.getHierarchy().getClass());
		assertSame(TestImpl3.class, actual.getTestInterface1().getClass());
	}

	@Test(expected = InjectionException.class)
	public void getInstance_withNoImplementations() {
		odinJector.getInstance(UnboundInterface.class);
	}

	@Test
	public void getOptionalInstance_withNoImplementations() {
		Optional<UnboundInterface> actual = odinJector.getOptionalInstance(UnboundInterface.class);

		assertFalse(actual.isPresent());
	}

	@Test
	public void getInstances_withNoImplementations() {
		List<UnboundInterface> actual = odinJector.getInstances(UnboundInterface.class);

		assertNotNull(actual);
		assertTrue(actual.isEmpty());
	}

	@Test
	public void getInstanceFromProvider() {
		odinJector.addContext(new ProviderCtx());
		TestInterface1 actual = odinJector.getInstance(TestInterface1.class);

		assertSame(TestImpl2.class, actual.getClass());
	}

	@Test(expected = InjectionException.class)
	public void getInstance_fromContext_withNoImplementations() {
		odinJector.getInstance(MyAltCtx.class, UnboundInterface.class);
	}

	@Test
	public void getOptionalInstance_fromContext_withNoImplementations() {
		Optional<UnboundInterface> actual = odinJector.getOptionalInstance(MyAltCtx.class, UnboundInterface.class);

		assertFalse(actual.isPresent());
	}

	@Test
	public void getInstances_fromContext_withNoImplementations() {
		List<UnboundInterface> actual = odinJector.getInstances(MyAltCtx.class, UnboundInterface.class);

		assertNotNull(actual);
		assertTrue(actual.isEmpty());
	}

	@Test
	public void customAnnotation() {
		odinJector.addAnnotation(CustomAnnotation.class, (ca, conf) -> {
			conf.addContext(MyAltCtx.class);
		});

		ClassWithCustomAnntation actaul = odinJector.getInstance(ClassWithCustomAnntation.class);

		assertSame(TestImpl2.class, actaul.getInterface().getClass());
	}

	@Test
	public void customAnnotation_onParentClass() {
		odinJector.addAnnotation(CustomAnnotation.class, (ca, conf) -> {
			conf.addContext(MyAltCtx.class);
		});

		ExtendingClassWithCustomAnnotation actaul = odinJector.getInstance(ExtendingClassWithCustomAnnotation.class);

		assertSame(TestImpl2.class, actaul.getInterface().getClass());
	}

	@Test
	public void fallbackProvider() {
		odinJector.setFallback((c) -> {
			if (c == UnboundInterface.class) {
				return new UnboundInterfaceImplementation();
			}
			throw new RuntimeException("Unexpected class fallback");
		});

		UnboundInterface actual = odinJector.getInstance(UnboundInterface.class);

		assertSame(UnboundInterfaceImplementation.class, actual.getClass());
	}

	@Test
	public void injectorBinding() {
		ClassWithInjectorInjected actual = odinJector.getInstance(ClassWithInjectorInjected.class);

		assertSame(TestImpl1.class, actual.getImplementation().getClass());
	}

	@Test
	public void wrapObjectUsingListener() {
		odinJector = OdinJector.create();
		TestInterface1 mock = Mockito.mock(TestInterface1.class);
		odinJector.addContext(new Context() {
			@Override
			public void configure(Binder binder) {
				binder.bindPackageToContext(TestImpl1.class.getPackage());
				binder.injectionListener((injectionContext) -> {
					injectionContext.wrap((obj) -> Mockito.spy(obj));
				});
			}
		});
		TestImpl1 actual = odinJector.getInstance(TestImpl1.class);

		actual.muh();
		verify(actual).muh();
	}

	@Test
	public void wrapObjectUsingListener_forCertainObjects() {
		odinJector = OdinJector.create();
		TestInterface1 mock = Mockito.mock(TestInterface1.class);
		odinJector.addContext(new Context() {
			@Override
			public void configure(Binder binder) {
				binder.bindPackageToContext(TestImpl1.class.getPackage());
				binder.bind(TestInterface1.class).to(TestImpl1.class);
				binder.injectionListener((injectionContext) -> {
					if(injectionContext.getTarget().hasAnnotation(Wrapped.class) && injectionContext.getClazz() == TestInterface1.class) {
						injectionContext.wrap((o) -> mock);
					}
				});
			}
		});
		AllInjectionTypes actual = odinJector.getInstance(AllInjectionTypes.class);

		actual.runAll();

		verify(mock, times(3)).muh();
	}

	@Test(expected = RuntimeException.class)
	public void wrapObjectUsingBindingResultListener_failOnMockInjected() {
		odinJector = OdinJector.create();
		TestInterface1 mock = Mockito.mock(TestInterface1.class);
		odinJector.addContext(new Context() {
			@Override
			public void configure(Binder binder) {
				binder.bindPackageToContext(TestImpl1.class.getPackage());
				binder.bind(TestInterface1.class).to(() -> Mockito.mock(TestInterface1.class));
				binder.bindingResultListener(brl -> {
					if (brl.getBoundClass().getSimpleName().contains("$MockitoMock$")) {
						throw new RuntimeException("This is a mock");
					}
				});
			}
		});
		AllInjectionTypes actual = odinJector.getInstance(AllInjectionTypes.class);
	}

//	@Test
	public void runAll() throws Throwable {
		List<Method> methods = new ArrayList<>();
		for (Method m : OdinJectorTest.class.getMethods()) {
			if (m.getAnnotation(Test.class) != null && !m.getName().equals("runAll")) {
				methods.add(m);
			}
		}
		long s = System.currentTimeMillis();
		int success = 0;
		int fail = 0;

		List<Thread> threads = new ArrayList<>();
		for(int i=0;i<50;i++) {
			Thread t = new Thread(() -> {
				Object previous = null;
				for(int x =0;x<1_000_000;x++) {
					TestImpl1 actual1 = odinJector.getInstance(TestImpl1.class);
					if (previous == null) {
						previous = actual1;
					} else {
						assertNotSame(previous, actual1);
					}
				}
			});
			threads.add(t);
		}
		threads.forEach(Thread::run);
		threads.forEach(t -> {
			try {
				t.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		});
		System.out.println(System.currentTimeMillis()-s);
		System.out.println(success+" vs "+fail);
		System.out.println(OdinJector.i);
	}
}