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
import org.spongepowered.api.data.key.Key;
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

public final class TypeTokenHelper {

    public static void main(String... args) {
        test(new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<?>>>() {});
        test(new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<CatalogType>>>() {});
        test(new TypeToken<Key<BaseValue<?>>>() {},
                new TypeToken<Key<BaseValue<? extends CatalogType>>>() {});
        test(new TypeToken<Key<BaseValue<Advancement>>>() {},
                new TypeToken<Key<BaseValue<Integer>>>() {});
        test(new TypeToken<Key<BaseValue<Slime>>>() {},
                new TypeToken<Key<BaseValue<? extends EnderDragon>>>() {});
        test(new TypeToken<Key<BaseValue<EnderDragon>>>() {},
                new TypeToken<Key<BaseValue<? extends Living>>>() {});

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
        if (type.equals(toType)) {
            return true;
        }
        if (toType instanceof Class) {
            return isAssignable(type, (Class<?>) toType);
        }
        if (toType instanceof ParameterizedType) {
            return isAssignable(type, (ParameterizedType) toType);
        }
        if (toType instanceof TypeVariable) {
            return isAssignable(type, (TypeVariable) toType);
        }
        if (toType instanceof WildcardType) {
            return isAssignable(type, (WildcardType) toType);
        }
        if (toType instanceof GenericArrayType) {
            return isAssignable(type, (GenericArrayType) toType);
        }
        throw new IllegalStateException("Unsupported type: " + type);
    }

    private static boolean isAssignable(Type type, Class<?> toType) {
        if (type instanceof Class) {
            final Class<?> other = (Class<?>) type;
            final Class<?> toEnclosing = toType.getEnclosingClass();
            if (toEnclosing != null && !Modifier.isStatic(toType.getModifiers())) {
                final Class<?> otherEnclosing = other.getEnclosingClass();
                if (otherEnclosing == null || !isAssignable(otherEnclosing, toEnclosing)) {
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
                if (otherEnclosing == null || !isAssignable(otherEnclosing, toEnclosing)) {
                    return false;
                }
            }
            return toType.isAssignableFrom((Class<?>) other.getRawType());
        }
        if (type instanceof TypeVariable) {
            final TypeVariable other = (TypeVariable) type;
            return allAssignable(type, other.getBounds());
        }
        if (type instanceof WildcardType) {
            final WildcardType other = (WildcardType) type;
            return allSupertypes(toType, other.getUpperBounds()) &&
                    allAssignable(toType, other.getLowerBounds());
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType other = (GenericArrayType) type;
            return toType.equals(Object.class) || (toType.isArray() &&
                    isAssignable(other.getGenericComponentType(), toType.getComponentType()));
        }
        throw new IllegalStateException("Unsupported type: " + type);
    }

    private static boolean isAssignable(Type type, ParameterizedType toType) {
        if (type instanceof Class) {
            final Class<?> other = (Class<?>) type;
            final Class<?> toRaw = (Class<?>) toType.getRawType();
            final Type toEnclosing = toType.getOwnerType();
            if (toEnclosing != null && !Modifier.isStatic(toRaw.getModifiers())) {
                final Class<?> otherEnclosing = other.getEnclosingClass();
                if (otherEnclosing == null || !isAssignable(otherEnclosing, toEnclosing)) {
                    return false;
                }
            }
            return toRaw.isAssignableFrom(other);
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType other = (ParameterizedType) type;
            final Class<?> otherRaw = (Class<?>) other.getRawType();
            final Class<?> toRaw = (Class<?>) toType.getRawType();
            if (!toRaw.isAssignableFrom(otherRaw)) {
                return false;
            }
            final Type toEnclosing = toType.getOwnerType();
            if (toEnclosing != null && !Modifier.isStatic(toRaw.getModifiers())) {
                final Type otherEnclosing = other.getOwnerType();
                if (otherEnclosing == null || !isAssignable(otherEnclosing, toEnclosing)) {
                    return false;
                }
            }
            final Type[] types = other.getActualTypeArguments();
            final Type[] toTypes = toType.getActualTypeArguments();
            if (types.length != toTypes.length) {
                return false;
            }
            for (int i = 0; i < types.length; i++) {
                if (!isAssignable(types[i], toTypes[i])) {
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
            return allSupertypes(toType, other.getUpperBounds()) &&
                    allAssignable(toType, other.getLowerBounds());
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType other = (GenericArrayType) type;
            final Class<?> rawType = (Class<?>) toType.getRawType();
            return rawType.equals(Object.class) || (rawType.isArray() &&
                    isAssignable(other.getGenericComponentType(), rawType.getComponentType()));
        }
        throw new IllegalStateException("Unsupported type: " + type);
    }

    private static boolean isAssignable(Type type, TypeVariable toType) {
        return allAssignable(type, toType.getBounds());
    }

    private static boolean isAssignable(Type type, WildcardType toType) {
        return allAssignable(type, toType.getUpperBounds()) &&
                allSupertypes(type, toType.getLowerBounds());
    }

    private static boolean isAssignable(Type type, GenericArrayType toType) {
        if (type instanceof Class) {
            final Class<?> other = (Class<?>) type;
            return other.isArray() && isAssignable(other.getComponentType(), toType.getGenericComponentType());
        }
        if (type instanceof ParameterizedType) {
            final ParameterizedType other = (ParameterizedType) type;
            final Class<?> rawType = (Class<?>) other.getRawType();
            return rawType.isArray() && isAssignable(rawType.getComponentType(), toType.getGenericComponentType());
        }
        if (type instanceof TypeVariable) {
            final TypeVariable other = (TypeVariable) type;
            return allSupertypes(toType, other.getBounds());
        }
        if (type instanceof WildcardType) {
            final WildcardType other = (WildcardType) type;
            return allSupertypes(toType, other.getUpperBounds()) &&
                    allAssignable(toType, other.getLowerBounds());
        }
        if (type instanceof GenericArrayType) {
            final GenericArrayType other = (GenericArrayType) type;
            return isAssignable(other.getGenericComponentType(), toType.getGenericComponentType());
        }
        throw new IllegalStateException("Unsupported type: " + type);
    }

    private static boolean allAssignable(Type type, Type[] bounds) {
        for (Type toType : bounds) {
            // Skip the Object class
            if (!toType.equals(Object.class) &&
                    !isAssignable(type, toType)) {
                return false;
            }
        }
        return true;
    }

    private static boolean allSupertypes(Type type, Type[] bounds) {
        for (Type toType : bounds) {
            // Skip the Object class
            if (!toType.equals(Object.class) &&
                    !isAssignable(toType, type)) {
                return false;
            }
        }
        return true;
    }

    private TypeTokenHelper() {
    }
}
