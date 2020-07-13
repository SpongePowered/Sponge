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

import com.google.common.base.Preconditions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.common.command.parameter.managed.standard.SpongeChoicesValueParameter;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SpongeDynamicChoicesBuilder<T> implements VariableValueParameters.DynamicChoicesBuilder<T> {

    @Nullable private Supplier<? extends Collection<String>> choices = null;
    @Nullable private Function<String, ? extends T> results = null;
    private boolean showInUsage = false;

    @Override
    public VariableValueParameters.@NonNull DynamicChoicesBuilder<T> setChoicesAndResults(@NonNull final Supplier<Map<String, ? extends T>> choices) {
        this.choices = () -> choices.get().keySet();
        this.results = x -> choices.get().get(x);
        return this;
    }

    @Override
    public VariableValueParameters.@NonNull DynamicChoicesBuilder<T> setChoices(final Supplier<? extends Collection<String>> choices) {
        this.choices = choices;
        return this;
    }

    @Override
    public VariableValueParameters.@NonNull DynamicChoicesBuilder<T> setResults(@NonNull final Function<String, ? extends T> results) {
        this.results = results;
        return this;
    }

    @Override
    public VariableValueParameters.@NonNull DynamicChoicesBuilder<T> setShowInUsage(final boolean showInUsage) {
        this.showInUsage = showInUsage;
        return this;
    }

    @Override
    @NonNull
    public ValueParameter<T> build() {
        Preconditions.checkNotNull(this.choices, "choices must not be null");
        Preconditions.checkNotNull(this.results, "results must not be null");
        return new SpongeChoicesValueParameter<>(this.choices, this.results, this.showInUsage);
    }

    @Override
    public VariableValueParameters.@NonNull DynamicChoicesBuilder<T> reset() {
        this.choices = null;
        this.results = null;
        this.showInUsage = false;
        return this;
    }

}
