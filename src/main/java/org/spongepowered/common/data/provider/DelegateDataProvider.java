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

import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class DelegateDataProvider<V extends Value<E>, E> implements DataProvider<V, E> {

    private final Key<V> key;
    private final List<DataProvider<V, E>> providers;

    DelegateDataProvider(Key<V> key, List<DataProvider<V, E>> providers) {
        this.providers = providers.stream().sorted(HierarchyComparator.COMPARATOR).collect(Collectors.toList());
        this.key = key;
    }

    public static class HierarchyComparator implements Comparator<DataProvider> {
        public static final HierarchyComparator COMPARATOR = new HierarchyComparator();

        public int compare(DataProvider c1, DataProvider c2) {
            if (c1.equals(c2)) {
                return 0;
            }
            final boolean firstKnown = c1 instanceof AbstractDataProvider.KnownHolderType;
            final boolean secondKnown = c2 instanceof AbstractDataProvider.KnownHolderType;
            if (firstKnown && secondKnown) {
                final Class<?> firstType = ((AbstractDataProvider.KnownHolderType) c1).getHolderType();
                final Class<?> secondType = ((AbstractDataProvider.KnownHolderType) c2).getHolderType();
                if (firstType.equals(secondType)) {
                    return 0;
                }
                if (firstType.isAssignableFrom(secondType)) {
                    return 1;
                }
                if (secondType.isAssignableFrom(firstType)) {
                    return -1;
                }
                return firstType.toString().compareTo(secondType.toString());
            }
            if (firstKnown) {
                return 1;
            }
            if (secondKnown) {
                return -1;
            }
            return 0;
        }

        private HierarchyComparator() {
        }
    }

    @Override
    public Key<V> key() {
        return this.key;
    }

    @Override
    public boolean allowsAsynchronousAccess(DataHolder dataHolder) {
        return this.providers.stream().allMatch(provider -> provider.allowsAsynchronousAccess(dataHolder));
    }

    @Override
    public Optional<E> get(DataHolder dataHolder) {
        return this.providers.stream()
                .map(provider -> provider.get(dataHolder))
                .filter(Optional::isPresent)
                .findFirst().flatMap(optional -> optional);
    }

    @Override
    public boolean isSupported(DataHolder dataHolder) {
        return this.providers.stream().anyMatch(provider -> provider.isSupported(dataHolder));
    }

    @Override
    public boolean isSupported(final Type dataHolder) {
        return this.providers.stream().anyMatch(provider -> provider.isSupported(dataHolder));
    }

    @Override
    public DataTransactionResult offer(DataHolder.Mutable dataHolder, E element) {
        return this.providers.stream()
                .map(provider -> provider.offer(dataHolder, element))
                .filter(result -> result.type() != DataTransactionResult.Type.FAILURE)
                .findFirst()
                .orElseGet(() -> DataTransactionResult.errorResult(Value.immutableOf(this.key, element)));
    }

    @Override
    public DataTransactionResult remove(DataHolder.Mutable dataHolder) {
        return this.providers.stream()
                .map(provider -> provider.remove(dataHolder))
                .filter(result -> result.type() != DataTransactionResult.Type.FAILURE)
                .findFirst()
                .orElseGet(DataTransactionResult::failNoData);
    }

    @Override
    public <I extends DataHolder.Immutable<I>> Optional<I> with(I immutable, E element) {
        return this.providers.stream()
                .map(provider -> provider.with(immutable, element))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }

    @Override
    public <I extends DataHolder.Immutable<I>> Optional<I> without(I immutable) {
        return this.providers.stream()
                .map(provider -> provider.without(immutable))
                .filter(Optional::isPresent)
                .findFirst()
                .orElseGet(() -> Optional.of(immutable));
    }
}
