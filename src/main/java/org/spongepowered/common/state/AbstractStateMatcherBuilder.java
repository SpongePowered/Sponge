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
package org.spongepowered.common.state;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.state.State;
import org.spongepowered.api.state.StateContainer;
import org.spongepowered.api.state.StateMatcher;
import org.spongepowered.api.state.StateProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractStateMatcherBuilder<S extends State<@NonNull S>, T extends StateContainer<@NonNull S>>
        implements StateMatcher.Builder<@NonNull S, @NonNull T> {

    @Nullable T type;
    final Collection<StateProperty<@NonNull ?>> requiredProperties = new ArrayList<>();
    final Map<StateProperty<@NonNull ?>, Object> properties = new HashMap<>();
    final Collection<KeyValueMatcher<?>> keyValueMatchers = new ArrayList<>();

    @Override
    public StateMatcher.@NonNull Builder<@NonNull S, @NonNull T> type(@NonNull final T type) {
        this.type = type;
        return this;
    }

    @Override
    public StateMatcher.@NonNull Builder<@NonNull S, @NonNull T> supportsStateProperty(@NonNull final StateProperty<@NonNull ?> stateProperty) {
        this.requiredProperties.add(stateProperty);
        return this;
    }

    @Override
    public <V extends Comparable<V>> StateMatcher.@NonNull Builder<@NonNull S, @NonNull T> stateProperty(
            @NonNull final StateProperty<@NonNull V> stateProperty, @NonNull final V value) {
        this.properties.put(stateProperty, value);
        return this;
    }

    @Override
    public StateMatcher.@NonNull Builder<@NonNull S, @NonNull T> matcher(@NonNull final KeyValueMatcher<?> matcher) {
        this.keyValueMatchers.add(matcher);
        return this;
    }

    @Override
    public StateMatcher.@NonNull Builder<@NonNull S, @NonNull T> reset() {
        this.type = null;
        this.properties.clear();
        this.requiredProperties.clear();
        this.keyValueMatchers.clear();
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public StateMatcher.@NonNull Builder<@NonNull S, @NonNull T> from(@NonNull final StateMatcher<@NonNull S> value) {
        final AbstractStateMatcherBuilder<S, T> that = ((AbstractStateMatcherBuilder<S, T>) value);
        this.type = that.type;
        this.properties.clear();
        this.properties.putAll(that.properties);
        this.requiredProperties.clear();
        this.requiredProperties.addAll(that.requiredProperties);
        this.keyValueMatchers.clear();
        this.keyValueMatchers.addAll(that.keyValueMatchers);
        return this;
    }

}
