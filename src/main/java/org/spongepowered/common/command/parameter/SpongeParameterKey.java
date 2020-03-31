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

import com.google.common.reflect.TypeToken;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.parameter.Parameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SpongeParameterKey<T> implements Parameter.Key<T> {

    private final static Map<Parameter.Key<?>, SpongeParameterKey<?>> keyCache = new HashMap<>();

    private final String key;
    private final TypeToken<T> typeToken;

    @SuppressWarnings("unchecked")
    public static <T> SpongeParameterKey<T> getSpongeKey(final Parameter.@NonNull Key<? super T> key) {
        if (key instanceof SpongeParameterKey) {
            return (SpongeParameterKey<T>) key;
        }

        return (SpongeParameterKey<T>) keyCache.computeIfAbsent(key, SpongeParameterKey::new);
    }

    private SpongeParameterKey(final Parameter.@NonNull Key<T> parameterKey) {
        this(parameterKey.key(), parameterKey.getTypeToken());
    }

    public SpongeParameterKey(@NonNull final String key, @NonNull final TypeToken<T> typeToken) {
        this.key = key;
        this.typeToken = typeToken;
    }

    @Override
    @NonNull
    public String key() {
        return this.key;
    }

    @Override
    @NonNull
    public TypeToken<T> getTypeToken() {
        return this.typeToken;
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
        return this.key.equals(that.key) && this.typeToken.equals(that.typeToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.key, this.typeToken);
    }

    @Override
    public String toString() {
        return "Key: " + this.key + ", Class " + this.typeToken.getType().getTypeName();
    }

    public static class Builder implements Parameter.Key.Builder {

        @Override
        public <T> Parameter.@NonNull Key<T> build(@NonNull final String key, @NonNull final TypeToken<T> typeToken) {
            return new SpongeParameterKey<>(key, typeToken);
        }

        @Override
        public Parameter.Key.@NonNull Builder reset() {
            return this;
        }

    }

}
