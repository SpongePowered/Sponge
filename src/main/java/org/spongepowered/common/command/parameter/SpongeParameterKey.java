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
package org.spongepowered.common.command.parameter;

import io.leangen.geantyref.GenericTypeReflector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.parameter.Parameter;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SpongeParameterKey<T> implements Parameter.Key<T> {

    private final static Map<Parameter.Key<?>, SpongeParameterKey<?>> keyCache = new HashMap<>();

    private final String key;
    private final Type type;

    @SuppressWarnings("unchecked")
    public static <T> SpongeParameterKey<T> getSpongeKey(final Parameter.@NonNull Key<? super T> key) {
        if (key instanceof SpongeParameterKey) {
            return (SpongeParameterKey<T>) key;
        }

        return (SpongeParameterKey<T>) keyCache.computeIfAbsent(key, SpongeParameterKey::new);
    }

    private SpongeParameterKey(final Parameter.@NonNull Key<T> parameterKey) {
        this.key = parameterKey.key();
        this.type = parameterKey.getType();
    }

    public SpongeParameterKey(final @NonNull String key, final @NonNull Type type) {
        this.key = key;
        this.type = type;
    }

    @Override
    @NonNull
    public String key() {
        return this.key;
    }

    @Override
    @NonNull
    public Type getType() {
        return this.type;
    }

    @Override
    public boolean isInstance(final Object value) {
        return value != null && GenericTypeReflector.erase(this.type).isInstance(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T cast(final Object value) {
        return (T) value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeParameterKey<?> that = (SpongeParameterKey<?>) o;
        return this.key.equals(that.key) && this.type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key, this.type);
    }

    @Override
    public String toString() {
        return "Key: " + this.key + ", Class " + this.type.getTypeName();
    }

}
