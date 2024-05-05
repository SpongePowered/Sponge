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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.common.command.parameter.managed.standard.SpongeTextValueParameter;
import org.spongepowered.common.util.Preconditions;

import java.util.Objects;
import java.util.function.Supplier;

public final class SpongeTextParameterBuilder implements VariableValueParameters.TextBuilder {

    private @Nullable ComponentSerializer<Component, ? extends Component, String> textSerializer;
    private boolean consumeAllArguments;

    @Override
    public VariableValueParameters.@NonNull TextBuilder serializer(final @NonNull ComponentSerializer<Component, ? extends Component, String> serializer) {
        Objects.requireNonNull(serializer, "The serializer cannot be null");
        return this.serializerSupplier(() -> serializer);
    }

    @Override
    public VariableValueParameters.@NonNull TextBuilder serializerSupplier(final @NonNull Supplier<ComponentSerializer<Component, ? extends Component, String>> serializerSupplier) {
        this.textSerializer = serializerSupplier.get();
        return this;
    }

    @Override
    public VariableValueParameters.@NonNull TextBuilder consumeAllArguments(final boolean allArguments) {
        this.consumeAllArguments = allArguments;
        return this;
    }

    @Override
    public ValueParameter<Component> build() throws IllegalStateException {
        Preconditions.checkState(this.textSerializer != null, "Text Serializer cannot be null.");
        return new SpongeTextValueParameter(this.textSerializer, this.consumeAllArguments);
    }

    @Override
    public VariableValueParameters.TextBuilder reset() {
        this.textSerializer = null;
        this.consumeAllArguments = false;
        return this;
    }
}
