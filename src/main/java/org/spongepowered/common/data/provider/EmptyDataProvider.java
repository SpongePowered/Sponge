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
package org.spongepowered.common.data.provider;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataProvider;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;

import java.util.Optional;

public class EmptyDataProvider<V extends Value<E>, E> implements DataProvider<V, E> {

    private final Key<V> key;

    public EmptyDataProvider(Key<V> key) {
        this.key = key;
    }

    @Override
    public Key<V> getKey() {
        return this.key;
    }

    @Override
    public boolean allowsAsynchronousAccess(DataHolder dataHolder) {
        return false;
    }

    @Override
    public Optional<E> get(DataHolder dataHolder) {
        return Optional.empty();
    }

    @Override
    public boolean isSupported(DataHolder dataHolder) {
        return false;
    }

    @Override
    public DataTransactionResult offer(DataHolder.Mutable dataHolder, E element) {
        return DataTransactionResult.failResult(Value.immutableOf(this.key, element));
    }

    @Override
    public DataTransactionResult remove(DataHolder.Mutable dataHolder) {
        return DataTransactionResult.failNoData();
    }

    @Override
    public <I extends DataHolder.Immutable<I>> Optional<I> with(I immutable, E element) {
        return Optional.empty();
    }

    @Override
    public <I extends DataHolder.Immutable<I>> Optional<I> without(I immutable) {
        return Optional.of(immutable);
    }
}
