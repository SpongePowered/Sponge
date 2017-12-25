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

import com.google.common.base.Joiner;
import com.google.common.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public final class TypeTokenHelper {

    public static TypeToken<?> withGenericTypes(Class<?> rawType, TypeToken<?>... genericTypes) {
        return TypeToken.of(new GenericTypeRawClass(rawType, genericTypes));
    }

    private static class GenericTypeRawClass implements ParameterizedType {

        private final Type[] types;
        private final Class<?> rawType;

        private GenericTypeRawClass(Class<?> rawType, TypeToken<?>... genericTypes) {
            this.types = new Type[genericTypes.length];
            for (int i = 0; i < genericTypes.length; i++) {
                this.types[i] = genericTypes[i].getType();
            }
            this.rawType = rawType;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return this.types.clone();
        }

        @Override
        public Type getRawType() {
            return this.rawType;
        }

        @Nullable
        @Override
        public Type getOwnerType() {
            return null;
        }
    }

    /**
     * Replaces all the unknown type variables with the
     * type that is still supported.
     * <p>For example, {@code org.spongepowered.api.util.Tuple<org.spongepowered.api.plugin.PluginContainer, ? super T>}
     * within the context where {@code T} extends {@code CatalogType} will return
     * {@code org.spongepowered.api.util.Tuple<org.spongepowered.api.plugin.PluginContainer,
     * ? super org.spongepowered.api.CatalogType>}.
     * <p>Removing this is required to check sub types of {@link TypeToken} that don't use any
     * generic parameters but are all defined.
     *
     * @param typeToken The type token
     * @return The result type token
     */
    public static TypeToken<?> removeGenericTypes(TypeToken<?> typeToken) {
        return TypeToken.of(wrap(typeToken.getType()));
    }

    private static Type wrap(Type type) {
        if (type instanceof TypeVariable) {
            return new TypeVariableImpl((TypeVariable) type);
        } else if (type instanceof ParameterizedType) {
            return new ParameterizedTypeImpl((ParameterizedType) type);
        } else if (type instanceof WildcardType) {
            return new WildcardTypeImpl((WildcardType) type);
        } else if (type instanceof GenericArrayType) {
            return new GenericArrayTypeImpl((GenericArrayType) type);
        }
        return type;
    }

    private static Type[] wrap(Type[] types) {
        final Type[] wrapped = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            wrapped[i] = wrap(types[i]);
        }
        return wrapped;
    }

    private static String typeToString(Type type) {
        return type instanceof Class ? ((Class) type).getName() : type.getTypeName();
    }

    private static class GenericArrayTypeImpl implements GenericArrayType {

        private final Type genericComponentType;

        private GenericArrayTypeImpl(GenericArrayType genericArrayType) {
            this.genericComponentType = wrap(genericArrayType.getGenericComponentType());
        }

        @Override
        public Type getGenericComponentType() {
            return this.genericComponentType;
        }

        @Override
        public String toString() {
            return typeToString(this.genericComponentType) + "[]";
        }
    }

    private static LinkedHashSet<Type> wrapWildcardBounds(Type[] bounds, boolean upper) {
        final LinkedHashSet<Type> wrapped = new LinkedHashSet<>();
        for (Type bound : bounds) {
            if (bound instanceof WildcardType) {
                final WildcardType wildcardType = (WildcardType) bound;
                wrapped.addAll(wrapWildcardBounds(upper ? wildcardType.getUpperBounds() :
                        wildcardType.getLowerBounds(), upper));
            } else if (bound instanceof TypeVariable) {
                wrapped.addAll(wrapWildcardBounds(((TypeVariable) bound).getBounds(), upper));
            } else {
                wrapped.add(bound);
            }
        }
        return wrapped;
    }

    private static class WildcardTypeImpl implements WildcardType {

        private final Type[] upper;
        private final Type[] lower;

        private WildcardTypeImpl(WildcardType wildcardType) {
            this.upper = wrapWildcardBounds(wildcardType.getUpperBounds(), true).toArray(new Type[0]);
            this.lower = wrapWildcardBounds(wildcardType.getLowerBounds(), false).toArray(new Type[0]);
        }

        @Override
        public Type[] getUpperBounds() {
            return this.upper.clone();
        }

        @Override
        public Type[] getLowerBounds() {
            return this.lower.clone();
        }

        @Override
        public String toString() {
            final Type[] bounds;
            final String base;
            if (this.lower.length > 0) {
                base = "? super ";
                bounds = this.lower;
            } else {
                if (this.upper.length == 0 || this.upper[0].equals(Object.class)) {
                    return "?";
                }
                base = "? extends ";
                bounds = this.upper;
            }
            return base + Joiner.on(" & ").join(Arrays.stream(bounds)
                    .map(TypeTokenHelper::typeToString).collect(Collectors.toList()));
        }
    }

    private static class TypeVariableImpl implements WildcardType {

        private final Type[] upper;

        private TypeVariableImpl(TypeVariable typeVariable) {
            this.upper = wrapWildcardBounds(typeVariable.getBounds(), true).toArray(new Type[0]);
        }

        @Override
        public Type[] getUpperBounds() {
            return this.upper.clone();
        }

        @Override
        public Type[] getLowerBounds() {
            return new Type[0];
        }

        @Override
        public String toString() {
            if (this.upper.length == 0 || this.upper[0].equals(Object.class)) {
                return "?";
            }
            return "? extends " + Joiner.on(" & ").join(Arrays.stream(this.upper)
                    .map(TypeTokenHelper::typeToString).collect(Collectors.toList()));
        }
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {

        private final Type[] arguments;
        private final Type ownerType;
        private final ParameterizedType wrapped;

        private ParameterizedTypeImpl(ParameterizedType wrapped) {
            this.wrapped = wrapped;
            this.ownerType = wrap(wrapped.getOwnerType());
            this.arguments = wrap(wrapped.getActualTypeArguments());
        }

        @Override
        public Type[] getActualTypeArguments() {
            return this.arguments;
        }

        @Override
        public Class<?> getRawType() {
            return (Class<?>) this.wrapped.getRawType();
        }

        @Nullable
        @Override
        public Type getOwnerType() {
            return this.ownerType;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            final Type ownerType = getOwnerType();
            if (ownerType != null) {
                builder.append(typeToString(ownerType));
                builder.append(".");
                if (ownerType instanceof ParameterizedTypeImpl) {
                    builder.append(getRawType().getName().replace(
                            ((ParameterizedTypeImpl) ownerType).getRawType().getName() + "$", ""));
                } else {
                    builder.append(getRawType().getName());
                }
            } else {
                builder.append(getRawType().getName());
            }
            if (this.arguments.length > 0) {
                builder.append("<");
                builder.append(StringUtils.join(Arrays.stream(this.arguments)
                        .map(Type::getTypeName).collect(Collectors.toList()), ", "));
                builder.append(">");
            }
            return builder.toString();
        }
    }
}
