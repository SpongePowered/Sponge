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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.common.command.parameter.managed.standard.SpongeLiteralValueParameter;

import java.util.Collection;
import java.util.function.Supplier;

public final class SpongeLiteralBuilder<T> implements VariableValueParameters.LiteralBuilder<T> {

    private @Nullable Supplier<? extends Collection<String>> strings;
    private @Nullable Supplier<T> returnValue;

    @Override
    public VariableValueParameters.@NonNull LiteralBuilder<T> literal(final @NonNull Supplier<? extends Collection<String>> literalSupplier) {
        this.strings = literalSupplier;
        return this;
    }

    @Override
    public VariableValueParameters.@NonNull LiteralBuilder<T> returnValue(final @NonNull Supplier<T> returnValueSupplier) {
        this.returnValue = returnValueSupplier;
        return this;
    }

    @Override
    public @NonNull ValueParameter<T> build() {
        return new SpongeLiteralValueParameter<>(this.strings, this.returnValue);
    }

    @Override
    public VariableValueParameters.@NonNull LiteralBuilder<T> reset() {
        this.strings = null;
        this.returnValue = null;
        return this;
    }
}
