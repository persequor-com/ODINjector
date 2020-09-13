package io.odinjector;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

public class TypeHelper {
	public <T> Class<T> get(Class<List<T>> listClass) {
		Type t = resolveType(listClass.getGenericSuperclass());
		try {
			return (Class<T>) Class.forName(t.getTypeName());
		} catch (ClassNotFoundException e) {
			throw new InjectionException(e);
		}
	}

	Type resolveType(Type toResolve) {
		// this implementation is made a little more complicated in an attempt to avoid object-creation
		while (true) {
			if (toResolve instanceof TypeVariable) {
				TypeVariable<?> original = (TypeVariable<?>) toResolve;
//				toResolve = MoreTypes.resolveTypeVariable(type, rawType, original);
//				if (toResolve == original) {
//					return toResolve;
//				}

			} else if (toResolve instanceof GenericArrayType) {
				GenericArrayType original = (GenericArrayType) toResolve;
				Type componentType = original.getGenericComponentType();
				Type newComponentType = resolveType(componentType);
//				return componentType == newComponentType ? original : Types.arrayOf(newComponentType);

			} else if (toResolve instanceof ParameterizedType) {
				ParameterizedType original = (ParameterizedType) toResolve;
				Type ownerType = original.getOwnerType();
				Type newOwnerType = resolveType(ownerType);
				boolean changed = newOwnerType != ownerType;

				Type[] args = original.getActualTypeArguments();
				for (int t = 0, length = args.length; t < length; t++) {
					Type resolvedTypeArgument = resolveType(args[t]);
					if (resolvedTypeArgument != args[t]) {
						if (!changed) {
							args = args.clone();
							changed = true;
						}
						args[t] = resolvedTypeArgument;
					}
				}

//				return changed
//						? Types.newParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args)
//						: original;

			} else if (toResolve instanceof WildcardType) {
				WildcardType original = (WildcardType) toResolve;
				Type[] originalLowerBound = original.getLowerBounds();
				Type[] originalUpperBound = original.getUpperBounds();

				if (originalLowerBound.length == 1) {
					Type lowerBound = resolveType(originalLowerBound[0]);
					if (lowerBound != originalLowerBound[0]) {
//						return Types.supertypeOf(lowerBound);
					}
				} else if (originalUpperBound.length == 1) {
					Type upperBound = resolveType(originalUpperBound[0]);
					if (upperBound != originalUpperBound[0]) {
//						return Types.subtypeOf(upperBound);
					}
				}
				return original;

			} else {
				return toResolve;
			}
		}
	}
}
