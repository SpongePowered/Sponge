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
package org.spongepowered.common.inject.plugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.spi.Element;
import com.google.inject.spi.ElementSource;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.PrivateElements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

final class BindingHelper {

    private final Binder binder;
    private final Map<Key<?>, Object> combinedKeys;

    BindingHelper(final Binder binder) {
        this.binder = binder;
        this.combinedKeys = new HashMap<>();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    void bind() {
        for (final Map.Entry<Key<?>, Object> key : this.combinedKeys.entrySet()) {
            Object value = key.getValue();
            if (value instanceof Set<?> set) {
                value = ImmutableSet.copyOf(set);
            } else if (value instanceof Iterable<?> iterable) {
                value = ImmutableList.copyOf(iterable);
            }
            this.binder.bind((Key) key.getKey()).toInstance(value);
        }
    }

    void bindFrom(final Injector fromInjector) {
        for (final Binding<?> binding : fromInjector.getBindings().values()) {
            if (!(binding.getSource() instanceof ElementSource elementSource) || elementSource.getDeclaringSource() == BindingHelper.class) {
                continue;
            }

            this.bindFrom(fromInjector, binding);
        }
    }

    @SuppressWarnings("rawtypes")
    void bindFrom(final Injector fromInjector, final Binding binding) {
        if (binding instanceof final ExposedBinding<?> exposedBinding) {
            final PrivateElements privateElements = exposedBinding.getPrivateElements();
            for (final Element privateElement : privateElements.getElements()) {
                if (!(privateElement instanceof final Binding privateBinding)
                    || !privateElements.getExposedKeys().contains(privateBinding.getKey())) {
                    continue;
                }

                this.bindFrom(fromInjector, privateBinding);
            }

            return;
        }

        this.bind(fromInjector, binding);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void bind(final Injector fromInjector, final Binding<?> binding) {
        final Key key = binding.getKey();
        final Class<?> clazz = key.getTypeLiteral().getRawType();
        if (Iterable.class.isAssignableFrom(clazz)) {
            final Collection destinationCollection;
            if (Set.class.isAssignableFrom(clazz)) {
                destinationCollection = this.getBindData(key, () -> new HashSet<>());
            } else {
                destinationCollection = this.getBindData(key, () -> new ArrayList<>());
            }
            final Iterable<Object> originalIterable = (Iterable<Object>) fromInjector.getInstance(key);
            for (final Object value : originalIterable) {
                destinationCollection.add(value);
            }
        } else {
            this.binder.bind(key).toProvider(() -> fromInjector.getInstance(key));
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getBindData(final Key<T> key, final Supplier<T> newValueSupplier) {
        return (T) this.combinedKeys.computeIfAbsent(key, $ -> newValueSupplier.get());
    }
}
