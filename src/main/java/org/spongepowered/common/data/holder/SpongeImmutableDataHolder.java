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
package org.spongepowered.common.data.holder;

import static java.util.Objects.requireNonNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.MergeFunction;
import org.spongepowered.api.data.value.Value;

import java.util.Optional;
import java.util.function.Function;

@SuppressWarnings({"unchecked"})
public interface SpongeImmutableDataHolder<I extends DataHolder.Immutable<I>> extends SpongeDataHolder, DataHolder.Immutable<I> {

    @Override
    default <E> Optional<I> transform(Key<? extends Value<E>> key, Function<E, E> function) {
        final DataProvider<?, E> provider = this.getProviderFor(key);
        return provider.get(this).flatMap(e -> {
            final E transformed = requireNonNull(function.apply(e));
            return provider.with((I) this, transformed);
        });
    }

    @Override
    default <E> Optional<I> with(Key<? extends Value<E>> key, E value) {
        return this.getProviderFor(key).with((I) this, value);
    }

    @Override
    default Optional<I> with(Value<?> value) {
        return this.getProviderFor((Key<Value<Object>>) value.getKey()).with((I) this, value.get());
    }

    @Override
    default Optional<I> without(Key<?> key) {
        return this.getProviderFor(key).without((I) this);
    }

    @Override
    default I mergeWith(I that, MergeFunction function) {
        I result = (I) this;
        if (function == MergeFunction.REPLACEMENT_PREFERRED) {
            for (final Value<?> value : that.getValues()) {
                final Optional<I> optionalResult = result.with(value);
                if (optionalResult.isPresent()) {
                    result = optionalResult.get();
                }
            }
        } else if (function == MergeFunction.ORIGINAL_PREFERRED) {
            for (final Value<?> value : that.getValues()) {
                if (this.get(value.getKey()).isPresent()) {
                    continue;
                }
                final Optional<I> optionalResult = result.with(value);
                if (optionalResult.isPresent()) {
                    result = optionalResult.get();
                }
            }
        } else {
            for (final Value<?> value : that.getValues()) {
                final Key<Value<Object>> key = (Key<Value<Object>>) value.getKey();
                final DataProvider<?, Object> provider = this.getProviderFor(key);
                @Nullable final Value<Object> original = provider.getValue(this).map(Value::asImmutable).orElse(null);
                final Value<Object> merged = function.merge(original, (Value<Object>) value);
                final Optional<I> optionalResult = result.with(merged);
                if (optionalResult.isPresent()) {
                    result = optionalResult.get();
                }
            }
        }
        return result;
    }
}
