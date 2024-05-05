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
package org.spongepowered.common.command.parameter.managed.builder;

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.common.command.parameter.managed.standard.SpongeChoicesValueParameter;
import org.spongepowered.common.util.Preconditions;

import java.util.HashMap;
import java.util.function.Supplier;

public final class SpongeStaticChoicesBuilder<T> implements VariableValueParameters.StaticChoicesBuilder<T> {

    private final HashMap<String, Supplier<? extends T>> choices = new HashMap<>();
    private boolean showInUsage = false;

    @Override
    public VariableValueParameters.@NonNull StaticChoicesBuilder<T> addChoices(
            final @NonNull Iterable<String> choices,
            final @NonNull Supplier<? extends T> returnedObjectSupplier) {

        for (final String string : choices) {
            this.choices.put(string, returnedObjectSupplier);
        }
        return this;
    }

    @Override
    public VariableValueParameters.@NonNull StaticChoicesBuilder<T> showInUsage(final boolean showInUsage) {
        this.showInUsage = showInUsage;
        return this;
    }

    @Override
    public @NonNull ValueParameter<T> build() {
        Preconditions.checkState(!this.choices.isEmpty(), "There must be at least one choice!");
        final ImmutableMap<String, Supplier<? extends T>> immutableChoices = ImmutableMap.copyOf(this.choices);
        return new SpongeChoicesValueParameter<>(immutableChoices, this.showInUsage, false);
    }

    @Override
    public VariableValueParameters.@NonNull StaticChoicesBuilder<T> reset() {
        this.choices.clear();
        this.showInUsage = false;
        return this;
    }

}
