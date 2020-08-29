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

import net.minecraft.state.IProperty;
import net.minecraft.state.IStateHolder;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.KeyValueMatcher;
import org.spongepowered.api.state.State;
import org.spongepowered.api.state.StateContainer;
import org.spongepowered.api.state.StateMatcher;
import org.spongepowered.api.state.StateProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractStateMatcher<S extends State<@NonNull S>, T extends StateContainer<@NonNull S>> implements StateMatcher<@NonNull S> {

    @MonotonicNonNull protected List<S> compatibleStates;

    final T type;
    final Map<StateProperty<@NonNull ?>, Object> properties;
    final Collection<KeyValueMatcher<?>> keyValueMatchers;
    final Collection<StateProperty<@NonNull ?>> requiredProperties;

    AbstractStateMatcher(final T type,
            final Collection<StateProperty<@NonNull ?>> requiredProperties,
            final HashMap<StateProperty<@NonNull ?>, Object> properties,
            final Collection<KeyValueMatcher<?>> keyValueMatchers) {
        this.type = type;
        this.requiredProperties = requiredProperties;
        this.properties = properties;
        this.keyValueMatchers = keyValueMatchers;
    }

    protected final boolean isValid(final IStateHolder<?> stateHolder) {
        for (final Map.Entry<StateProperty<@NonNull ?>, Object> entry : this.properties.entrySet()) {
            final IProperty<?> property = (IProperty<?>) entry.getKey();
            final Object value = stateHolder.getValues().get(property);
            if (value == null || !value.equals(entry.getValue())) {
                return false;
            }
        }
        for (final StateProperty<@NonNull ?> entry : this.requiredProperties) {
            final IProperty<?> property = (IProperty<?>) entry;
            if (stateHolder.getValues().get(property) == null) {
                return false;
            }
        }
        final DataHolder dataHolder = (DataHolder) stateHolder;
        for (final KeyValueMatcher<?> valueMatcher : this.keyValueMatchers) {
            if (!this.matches(dataHolder, valueMatcher)) {
                return false;
            }
        }
        return true;
    }

    private <V> boolean matches(final DataHolder holder, final KeyValueMatcher<V> keyValueMatcher) {
        return holder.get(keyValueMatcher.getKey()).map(keyValueMatcher::matches).orElse(false);
    }

}
