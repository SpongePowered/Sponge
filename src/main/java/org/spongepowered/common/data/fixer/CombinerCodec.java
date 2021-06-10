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
package org.spongepowered.common.data.fixer;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class CombinerCodec<A, B> implements Codec<A> {

    private final Codec<A> first;
    private final Codec<B> second;
    private final BiFunction<A, B, A> decodeAction;
    private final Function<A, B> encodeAction;

    public CombinerCodec(final Codec<A> first, final Codec<B> second, final BiFunction<A, B, A> decodeAction, Function<A, B> encodeAction) {
        this.first = first;
        this.second = second;
        this.decodeAction = decodeAction;
        this.encodeAction = encodeAction;
    }

    @Override
    public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
        final DataResult<Pair<A, T>> firstResult = this.first.decode(ops, input);
        final DataResult<Pair<B, T>> secondResult = this.second.decode(ops, input);
        // TODO Error verification
        if (firstResult.result().isEmpty()) {
            return firstResult;
        }
        return firstResult.map(res -> res.mapFirst(val -> this.decodeAction.apply(val, secondResult.result().get().getFirst())));
    }

    @Override
    public <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix) {
        final DataResult<T> firstResult = this.first.encode(input, ops, prefix);
        // TODO Error verification
        if (firstResult.result().isEmpty()) {
            return firstResult;
        }
        final DataResult<T> secondResult = this.second.encode(this.encodeAction.apply(input), ops, prefix);
        if (secondResult.result().isEmpty()) {
            return secondResult;
        }
        return this.merge(ops, firstResult, secondResult);
    }

    public Codec<A> first() {
        return this.first;
    }

    public Codec<B> second() {
        return this.second;
    }

    protected abstract <T> DataResult<T> merge(DynamicOps<T> adapter, DataResult<T> first, DataResult<T> second);
}
