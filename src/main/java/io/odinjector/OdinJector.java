/* Copyright (C) Persequor ApS - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Persequor Development Team <partnersupport@persequor.com>, 2020-09-12
 */
package io.odinjector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("rawtypes")
public class OdinJector {
	private final Providers providers;
	private final Yggdrasill yggdrasill;

	private OdinJector() {
		yggdrasill = new Yggdrasill();
		providers = new Providers(yggdrasill, this);
	}

	public static OdinJector create() {
		return new OdinJector();
	}

	public OdinJector addContext(Context context) {
		yggdrasill.addContext(context);
		return this;
	}

	public OdinJector addDynamicContext(Context context) {
		yggdrasill.addDynamicContext(context);
		return this;
	}

	public <T> T getInstance(Class<T> type) {
		return getInstance(InjectionContext.get(new ArrayList<>(), type));
	}

	public <T> Optional<T> getOptionalInstance(Class<T> type) {
		return Optional.ofNullable(getInstance(InjectionContext.get(new ArrayList<>(), type, InjectionOptions.get().optional())));
	}

	public <T> T getInstance(Class<?> context, Class<T> type) {
		return getInstance(InjectionContext.get(new ArrayList<>(yggdrasill.getDynamicContexts(Collections.singletonList(context))), type));
	}

	public <T> Optional<T> getOptionalInstance(Class<?> context, Class<T> type) {
		return Optional.ofNullable(getInstance(InjectionContext.get(new ArrayList<>(yggdrasill.getDynamicContexts(Collections.singletonList(context))), type, InjectionOptions.get().optional())));
	}

	public <T> List<T> getInstances(Class<T> type) {
		return getInstances(InjectionContext.get(new ArrayList<>(), type, InjectionOptions.get().optional()));
	}

	public <T> List<T> getInstances(Class<?> context, Class<T> type) {
		return getInstances(InjectionContext.get(new ArrayList<>(yggdrasill.getDynamicContexts(Collections.singletonList(context))), type, InjectionOptions.get().optional()));
	}

	@SuppressWarnings("unchecked")
	<T> T getInstance(InjectionContext<T> injectionContext) {
		return providers.get(injectionContext).get();
	}

	@SuppressWarnings("unchecked")
	<T> List<T> getInstances(InjectionContext<T> injectionContext) {
		return providers.getAll(injectionContext).get();
	}




}
