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
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.common.command.parameter.managed.standard.SpongeCatalogedElementValueParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class SpongeRegistryEntryParameterBuilder<T>
        implements VariableValueParameters.RegistryEntryBuilder<T> {

    private final List<Function<CommandContext, @Nullable RegistryHolder>> registryFunctions = new ArrayList<>();
    private final RegistryType<? extends T> registryType;
    private final List<String> prefixes = new ArrayList<>();

    public SpongeRegistryEntryParameterBuilder(final RegistryType<? extends T> registryType) {
        this.registryType = registryType;
    }

    @Override
    public VariableValueParameters.@NonNull RegistryEntryBuilder<T> addHolderFunction(
            final @NonNull Function<CommandContext, @Nullable RegistryHolder> holderFunction) {
        this.registryFunctions.add(holderFunction);
        return this;
    }

    @Override
    public VariableValueParameters.@NonNull RegistryEntryBuilder<T> defaultNamespace(final @NonNull String prefix) {
        this.prefixes.add(Objects.requireNonNull(prefix, "Prefix cannot be null!"));
        return this;
    }

    @Override
    public @NonNull ValueParameter<T> build() {
        if (this.registryFunctions.isEmpty()) {
            throw new IllegalStateException("No RegistryHolder functions were supplied.");
        }
        return new SpongeCatalogedElementValueParameter<>(new ArrayList<>(this.prefixes), new ArrayList<>(this.registryFunctions), this.registryType);
    }

    @Override
    public VariableValueParameters.@NonNull RegistryEntryBuilder<T> reset() {
        this.prefixes.clear();
        return this;
    }

}
