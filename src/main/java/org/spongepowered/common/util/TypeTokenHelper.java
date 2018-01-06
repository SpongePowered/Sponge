/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.util;

import com.google.common.reflect.TypeToken;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.data.DataRegistration;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.complex.EnderDragon;
import org.spongepowered.api.entity.living.monster.Slime;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import javax.annotation.Nullable;

@SuppressWarnings("unchecked")
public final class TypeTokenHelper {

    public static void main(String... args) {
        test(new TypeToken<Key<?>>() {},
                new TypeToken<Key<BaseValue<?>>>() {});
        test(new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<?>>>() {});
        test(new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<CatalogType>>>() {});
        test(new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<? extends CatalogType>>>() {});
        test(new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<? extends Advancement>>>() {});
        test(new TypeToken<Key<BaseValue<Advancement>>>() {},
                new TypeToken<Key<BaseValue<Integer>>>() {});
        test(new TypeToken<Key<BaseValue<Slime>>>() {},
                new TypeToken<Key<BaseValue<? extends EnderDragon>>>() {});
        test(new TypeToken<Key<BaseValue<EnderDragon>>>() {},
                new TypeToken<Key<BaseValue<? extends Living>>>() {});
        test(new TypeToken<Key<BaseValue<EnderDragon>>>() {},
                new TypeToken<Key<BaseValue<? extends Living>>>() {});
        test(TypeToken.of(Key.class),
                new TypeToken<Key<BaseValue<? extends Living>>>() {});

        test(new TypeToken<DataRegistration>() {},
                new TypeToken<DataRegistration<?,?>>() {});
        test(new TypeToken<DataRegistration>() {},
                new TypeToken<DataRegistration<LoreData,?>>() {});
        test(new TypeToken<DataRegistration<?,?>>() {},
                new TypeToken<DataRegistration<LoreData,?>>() {});

        // Enclosing classes testing
        test(new TypeToken<A<Object>.B<Value<Double>>>() {},
                new TypeToken<A<Object>.B<Value<? extends Number>>>() {});
        test(new TypeToken<A<Key<BaseValue<EnderDragon>>>.B<Value<Double>>>() {},
                new TypeToken<A<Key<BaseValue<Slime>>>.B<Value<? extends Number>>>() {});
        test(new TypeToken<A<Key<BaseValue<EnderDragon>>>.B<Value<Double>>>() {},
                new TypeToken<A<Key<BaseValue<? extends Living>>>.B<Value<? extends Number>>>() {});
    }

    private static class A<T> {

        private class B<V> {
        }
    }

    private static void test(TypeToken<?> a, TypeToken<?> b) {
        System.out.printf("{\n\tA: %s\n\tB: %s\n\tAB: %s\n\tBA: %s\n}\n", a, b, isAssignable(a, b), isAssignable(b, a));
    }

    public static boolean isAssignable(TypeToken<?> type, TypeToken<?> toType) {
        return isAssignable(type.getType(), toType.getType());
    }

    public static boolean isAssignable(Type type, Type toType) {
        return isAssignable(type, toType, null, 0);
    }

    private static boolean isAssignable(Type type, Type toType, @Nullable Type parent, int index) {
        if (type.equals(toType)) {
            return true;
        }
        if (toType instanceof Class) {
            return isAssignable(type, (Class<?>) toType, parent, index);
        }
        if (toType instanceof ParameterizedType) {
            return isAssignable(type, (ParameterizedType) toType, parent, index);
        }
        if (toType instanceof TypeVariable) {
            return isAssignable(type, (TypeVariable) toType, parent, index);
        }
        if (toType instanceof WildcardType) {
            return isAssignable(type, (WildcardType) toType, parent, index);
        }
        if (toType instanceof GenericArrayType) {
            return isAssignable(type, (GenericArrayType) toType, parent, index);
        }
        throw new IllegalStateException("Unsupported type: " + type);
    }

    private static boolean isAssignable(Type type, Class<?> toType, @Nullable Type parent, int index) {
        if (type instanceof Class) {
            final Class<?> other = (Class<?>) type;
            final Class<?> toEnclosing = toType.getEnclosingClass();
            if (toEnclosing != null && !Modifier.isStatic(toType.getModifiers())) {
                final Class<?> otherEnclosing = other.getEnclosingClass();
                if (otherEnclosing == null || !isAssignable(otherEnclosing, toEnclosing, null, 0)) {
                    return false;
                }
            }
            return toType.isAssignableFrom(other);
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType other = (ParameterizedType) type;
            final Class<?> toEnclosing = toType.getEnclosingClass();
            if (toEnclosing != null && !Modifier.isStatic(toType.getModifiers())) {
                final Type otherEnclosing = other.getOwnerType();
                if (otherEnclosing == null || !isAssignable(otherEnclosing, toEnclosing, null, 0)) {
                    return false;
                }
            }
            return toType.isAssignableFrom((Class<?>) other.getRawType());
        }
        if (type instanceof TypeVariable) {
            final TypeVariable other = (TypeVariable) type;
            return allSupertypes(type, other.getBounds());
        }
        if (type instanceof WildcardType) {
            final WildcardType other = (WildcardType) type;
            return allWildcardSupertypes(toType, other.getUpperBounds(), parent, index) &&
                    allAssignable(toType, other.getLowerBounds());
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType other = (GenericArrayType) type;
            return toType.equals(Object.class) || (toType.isArray() &&
                    isAssignable(other.getGenericComponentType(), toType.getComponentType(), parent, index));
        }
        throw new IllegalStateException("Unsupported type: " + type);
    }

    private static boolean isAssignable(Type type, ParameterizedType toType, @Nullable Type parent, int index) {
        if (type instanceof Class) {
            final Class<?> other = (Class<?>) type;
            final Class<?> toRaw = (Class<?>) toType.getRawType();
            final Type toEnclosing = toType.getOwnerType();
            if (toEnclosing != null && !Modifier.isStatic(toRaw.getModifiers())) {
                final Class<?> otherEnclosing = other.getEnclosingClass();
                if (otherEnclosing == null || !isAssignable(otherEnclosing, toEnclosing, null, 0)) {
                    return false;
                }
            }
            if (!toRaw.isAssignableFrom(other)) {
                return false;
            }
            // Check if the default generic parameters match the parameters
            // of the parameterized type
            final Type[] toTypes = toType.getActualTypeArguments();
            final TypeVariable[] types = toRaw.getTypeParameters();
            if (types.length != toTypes.length) {
                return false;
            }
            for (int i = 0; i < types.length; i++) {
                if (!isAssignable(types[i], toTypes[i], other, i)) {
                    return false;
                }
            }
            return true;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType other = (ParameterizedType) type;
            final Class<?> otherRaw = (Class<?>) other.getRawType();
            final Class<?> toRaw = (Class<?>) toType.getRawType();
            if (!toRaw.isAssignableFrom(otherRaw)) {
                return false;
            }
            final Type toEnclosing = toType.getOwnerType();
            if (toEnclosing != null && !Modifier.isStatic(toRaw.getModifiers())) {
                final Type otherEnclosing = other.getOwnerType();
                if (otherEnclosing == null || !isAssignable(otherEnclosing, toEnclosing, null, 0)) {
                    return false;
                }
            }
            final Type[] types;
            if (otherRaw.equals(toRaw)) {
                types = other.getActualTypeArguments();
            } else {
                // Get the type parameters based on the super class
                other = (ParameterizedType) TypeToken.of(type).getSupertype((Class) toRaw).getType();
                types = other.getActualTypeArguments();
            }
            final Type[] toTypes = toType.getActualTypeArguments();
            if (types.length != toTypes.length) {
                return false;
            }
            for (int i = 0; i < types.length; i++) {
                if (!isAssignable(types[i], toTypes[i], other, i)) {
                    return false;
                }
            }
            return true;
        }
        if (type instanceof TypeVariable) {
            final TypeVariable other = (TypeVariable) type;
            return allSupertypes(toType, other.getBounds());
        }
        if (type instanceof WildcardType) {
            final WildcardType other = (WildcardType) type;
            return allWildcardSupertypes(toType, other.getUpperBounds(), parent, index) &&
                    allAssignable(toType, other.getLowerBounds());
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType other = (GenericArrayType) type;
            final Class<?> rawType = (Class<?>) toType.getRawType();
            return rawType.equals(Object.class) || (rawType.isArray() &&
                    isAssignable(other.getGenericComponentType(), rawType.getComponentType(), parent, index));
        }
        throw new IllegalStateException("Unsupported type: " + type);
    }

    private static boolean isAssignable(Type type, TypeVariable toType, @Nullable Type parent, int index) {
        return allAssignable(type, toType.getBounds());
    }

    private static boolean isAssignable(Type type, WildcardType toType, @Nullable Type parent, int index) {
        return allWildcardAssignable(type, toType.getUpperBounds(), parent, index) &&
                allSupertypes(type, toType.getLowerBounds());
    }

    private static boolean isAssignable(Type type, GenericArrayType toType, @Nullable Type parent, int index) {
        if (type instanceof Class) {
            final Class<?> other = (Class<?>) type;
            return other.isArray() && isAssignable(other.getComponentType(), toType.getGenericComponentType(), parent, index);
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType other = (ParameterizedType) type;
            final Class<?> rawType = (Class<?>) other.getRawType();
            return rawType.isArray() && isAssignable(rawType.getComponentType(), toType.getGenericComponentType(), parent, index);
        }
        if (type instanceof TypeVariable) {
            final TypeVariable other = (TypeVariable) type;
            return allSupertypes(toType, other.getBounds());
        }
        if (type instanceof WildcardType) {
            final WildcardType other = (WildcardType) type;
            return allWildcardSupertypes(toType, other.getUpperBounds(), parent, index) &&
                    allAssignable(toType, other.getLowerBounds());
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType other = (GenericArrayType) type;
            return isAssignable(other.getGenericComponentType(), toType.getGenericComponentType(), parent, index);
        }
        throw new IllegalStateException("Unsupported type: " + type);
    }

    private static Type[] processBounds(Type[] bounds, @Nullable Type parent, int index) {
        if (bounds.length == 0 ||
                (bounds.length == 1 && bounds[0].equals(Object.class))) {
            Class<?> theClass = null;
            if (parent instanceof Class) {
                theClass = (Class<?>) parent;
            } else if (parent instanceof ParameterizedType) {
                theClass = (Class<?>) ((ParameterizedType) parent).getRawType();
            }
            if (theClass != null) {
                final TypeVariable[] typeVariables = theClass.getTypeParameters();
                bounds = typeVariables[index].getBounds();
                // Strip the new bounds down
                for (int i = 0; i < bounds.length; i++) {
                    if (bounds[i] instanceof TypeVariable ||
                            bounds[i] instanceof WildcardType ||
                            bounds[i] instanceof GenericArrayType) { // No idea how to handle this type
                        bounds[i] = Object.class;
                    } else if (bounds[i] instanceof ParameterizedType) {
                        bounds[i] = ((ParameterizedType) bounds[i]).getRawType();
                    }
                }
            }
        }
        return bounds;
    }

    private static boolean allWildcardSupertypes(Type type, Type[] bounds, @Nullable Type parent, int index) {
        return allSupertypes(type, processBounds(bounds, parent, index));
    }

    private static boolean allWildcardAssignable(Type type, Type[] bounds, @Nullable Type parent, int index) {
        return allAssignable(type, processBounds(bounds, parent, index));
    }

    private static boolean allAssignable(Type type, Type[] bounds) {
        for (Type toType : bounds) {
            // Skip the Object class
            if (!isAssignable(type, toType, null, 0)) {
                return false;
            }
        }
        return true;
    }

    private static boolean allSupertypes(Type type, Type[] bounds) {
        for (Type toType : bounds) {
            // Skip the Object class
            if (!isAssignable(toType, type, null, 0)) {
                return false;
            }
        }
        return true;
    }

    private TypeTokenHelper() {
    }
}
