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
package org.spongepowered.common.command.parameter.modifier.builder;

import com.google.common.base.Preconditions;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameterModifiers;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.common.command.parameter.modifier.DefaultValueModifier;

import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

public class SpongeDefaultValueModifierBuilder implements VariableValueParameterModifiers.DefaultValueModifierBuilder {

    @Nullable private Function<Cause, Optional<?>> defaultValueFunction = null;

    @Override
    public VariableValueParameterModifiers.DefaultValueModifierBuilder setDefaultValueFunction(Function<Cause, Optional<?>> defaultValueFunction) {
        this.defaultValueFunction = defaultValueFunction;
        return this;
    }

    @Override
    public VariableValueParameterModifiers.DefaultValueModifierBuilder from(ValueParameterModifier value) {
        Preconditions.checkArgument(value instanceof DefaultValueModifier, "value must be a DefaultValueModifier");
        this.defaultValueFunction = ((DefaultValueModifier) value).getDefaultValueFunction();
        return this;
    }

    @Override
    public VariableValueParameterModifiers.DefaultValueModifierBuilder reset() {
        this.defaultValueFunction = null;
        return this;
    }

    @Override
    public ValueParameterModifier build() {
        Preconditions.checkState(this.defaultValueFunction != null, "defaultValueFunction");
        return new DefaultValueModifier(this.defaultValueFunction);
    }

}
