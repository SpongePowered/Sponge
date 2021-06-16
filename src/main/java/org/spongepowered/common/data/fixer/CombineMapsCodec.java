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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class CombineMapsCodec<A, B> extends MapCodec<A> {

    private final MapCodec<A> first;
    private final Codec<B> second;
    private final BiFunction<A, B, A> decodeAction;
    private final Function<A, B> encodeAction;

    public <T> CombineMapsCodec(final Codec<A> first, final Codec<B> second, final BiFunction<A, B, A> decodeAction, Function<A, B> encodeAction) {
        this.first = ((MapCodecCodec<A>) first).codec();
        this.second = second;
        this.decodeAction = decodeAction;
        this.encodeAction = encodeAction;
    }

    @Override
    public <T> Stream<T> keys(final DynamicOps<T> ops) {
        final Stream<T> keys = this.first.keys(ops);
        return Stream.concat(keys, Stream.of(ops.createString(this.mergeKey())));
    }

    @Override
    public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final DataResult<A> firstResult = this.first.decode(ops, input);
        if (!firstResult.result().isPresent()) {
            return firstResult;
        }
        final T rawSecondData = input.get(this.mergeKey());
        if (rawSecondData == null) {
            return firstResult;
        }
        final DataResult<Pair<B, T>> secondResult = this.second.decode(ops, rawSecondData);
        if (!secondResult.result().isPresent()) {
            return firstResult;
        }
        return firstResult.map(val -> this.decodeAction.apply(val, secondResult.result().get().getFirst()));
    }

    @Override
    public <T> RecordBuilder<T> encode(final A input, final DynamicOps<T> ops, final RecordBuilder<T> prefix) {
        final RecordBuilder<T> firstResult = this.first.encode(input, ops, prefix);
        final DataResult<T> secondResult = this.second.encode(this.encodeAction.apply(input), ops, null);

        // Need an actual result and not a builder:
        firstResult.add(this.mergeKey(), secondResult);

        return firstResult;
    }

    protected abstract String mergeKey();
}
