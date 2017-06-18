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
package org.spongepowered.common.command.parameter.value.builder;

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.parameter.managed.ValueParameter;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameters;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.common.command.parameter.value.TextValueParameter;

import java.util.function.Supplier;

import javax.annotation.Nullable;

public class SpongeTextValueParameterBuilder implements VariableValueParameters.TextBuilder {

    @Nullable private Supplier<TextSerializer> serializerSupplier;
    private boolean allRemaining = false;

    @Override
    public VariableValueParameters.TextBuilder from(ValueParameter value) {
        Preconditions.checkArgument(value instanceof TextValueParameter, "The parameter must be a TextValueParameter");
        this.serializerSupplier = ((TextValueParameter) value).getSerializerSupplier();
        this.allRemaining = ((TextValueParameter) value).isAllRemaining();
        return this;
    }

    @Override
    public VariableValueParameters.TextBuilder reset() {
        this.serializerSupplier = null;
        this.allRemaining = false;
        return this;
    }

    @Override
    public VariableValueParameters.TextBuilder setSerializer(TextSerializer serializer) {
        this.serializerSupplier = () -> serializer;
        return this;
    }

    @Override
    public VariableValueParameters.TextBuilder setSerializerSupplier(Supplier<TextSerializer> serializerSupplier) {
        this.serializerSupplier = serializerSupplier;
        return this;
    }

    @Override
    public VariableValueParameters.TextBuilder setConsumeAllArguments(boolean allArguments) {
        this.allRemaining = allArguments;
        return this;
    }

    @Override
    public ValueParameter build() throws IllegalStateException {
        if (this.serializerSupplier == null) {
            throw new IllegalStateException("The TextSerializer must be set");
        }

        return new TextValueParameter(this.serializerSupplier, this.allRemaining);
    }
}
