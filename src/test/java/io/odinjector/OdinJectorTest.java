package io.odinjector;

import io.odinjector.testclasses.AltHierarchyImpl;
import io.odinjector.testclasses.ClassWithCustomAnntation;
import io.odinjector.testclasses.ClassWithMultipleContexts;
import io.odinjector.testclasses.ClassWithNonRecursiveHierarchialContext;
import io.odinjector.testclasses.ClassWithInterfaceInjection;
import io.odinjector.testclasses.ClassWithListInjection;
import io.odinjector.testclasses.ClassWithProviderInjection;
import io.odinjector.testclasses.ClassWithRecursiveHierarchialContext;
import io.odinjector.testclasses.ContextualDependencies;
import io.odinjector.testclasses.ContextualDependenciesWithMarker;
import io.odinjector.testclasses.CustomAnnotation;
import io.odinjector.testclasses.ExtendingClassWithCustomAnnotation;
import io.odinjector.testclasses.Hierarchy;
import io.odinjector.testclasses.InterfaceForClassWithNonRecursiveHierarchialContext;
import io.odinjector.testclasses.MyAltCtx;
import io.odinjector.testclasses.MyAltCtxWithMarker;
import io.odinjector.testclasses.MyCtx;
import io.odinjector.testclasses.MyMultipleBindingsCtx;
import io.odinjector.testclasses.MyOtherAltCtx;
import io.odinjector.testclasses.ProviderCtx;
import io.odinjector.testclasses.SingletonCtx;
import io.odinjector.testclasses.SingletonImpl;
import io.odinjector.testclasses.TestImpl1;
import io.odinjector.testclasses.TestImpl2;
import io.odinjector.testclasses.TestImpl3;
import io.odinjector.testclasses.TestInterface1;
import io.odinjector.testclasses.UnboundInterface;
import io.odinjector.testclasses.UnboundInterfaceImplementation;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

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
}