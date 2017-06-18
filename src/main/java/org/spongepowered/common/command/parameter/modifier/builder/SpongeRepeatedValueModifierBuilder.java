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
import org.spongepowered.api.command.parameter.managed.ValueParameterModifier;
import org.spongepowered.api.command.parameter.managed.standard.VariableValueParameterModifiers;
import org.spongepowered.common.command.parameter.modifier.RepeatedModifier;

public class SpongeRepeatedValueModifierBuilder implements VariableValueParameterModifiers.RepeatedValueModifierBuilder {

    private int numberOfTimes = 0; // Zero indicates the default, and is not valid

    @Override
    public VariableValueParameterModifiers.RepeatedValueModifierBuilder setNumberOfTimes(int numberOfTimes) {
        Preconditions.checkArgument(numberOfTimes > 0, "numberOfTimes must be positive");
        this.numberOfTimes = numberOfTimes;
        return this;
    }

    @Override
    public VariableValueParameterModifiers.RepeatedValueModifierBuilder from(ValueParameterModifier value) {
        Preconditions.checkState(value instanceof RepeatedModifier, "value must be a RepeatedModifier");
        this.numberOfTimes = ((RepeatedModifier) value).getNumberOfRepetitions();
        return this;
    }

    @Override
    public VariableValueParameterModifiers.RepeatedValueModifierBuilder reset() {
        this.numberOfTimes = 0;
        return this;
    }

    @Override
    public ValueParameterModifier build() {
        Preconditions.checkState(this.numberOfTimes > 0, "numberOfTimes must be set");
        return new RepeatedModifier(this.numberOfTimes);
    }

}
