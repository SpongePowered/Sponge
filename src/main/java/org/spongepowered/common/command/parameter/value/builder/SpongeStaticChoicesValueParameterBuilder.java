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
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.command.parameter.value.ChoicesValueParameter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SpongeStaticChoicesValueParameterBuilder implements VariableValueParameters.StaticChoicesBuilder {

    private final Map<String, Supplier<?>> choices = new HashMap<>();

    private boolean showInUsage = true;

    @Override
    public VariableValueParameters.StaticChoicesBuilder choices(Iterable<String> choices, Supplier<?> returnedObjectSupplier) {
        for (String choice : choices) {
            this.choices.put(choice, returnedObjectSupplier);
        }

        return this;
    }

    @Override
    public VariableValueParameters.StaticChoicesBuilder setShowInUsage(boolean showInUsage) {
        this.showInUsage = showInUsage;
        return this;
    }

    @Override
    public VariableValueParameters.StaticChoicesBuilder from(ValueParameter value) {
        Preconditions.checkArgument(value instanceof ChoicesValueParameter, "Must be an instance of the ChoicesValueParameter");
        ChoicesValueParameter cvp = (ChoicesValueParameter) value;
        this.choices.clear();
        this.choices.putAll(cvp.getChoices());
        this.showInUsage = cvp.getIncludeChoicesInUsage() != Tristate.FALSE;
        return this;
    }

    @Override
    public VariableValueParameters.StaticChoicesBuilder reset() {
        this.choices.clear();
        this.showInUsage = true;
        return this;
    }

    @Override
    public ValueParameter build() {
        Preconditions.checkState(!this.choices.isEmpty(), "choices");
        return new ChoicesValueParameter(this.choices, this.showInUsage ? Tristate.UNDEFINED : Tristate.FALSE);
    }

}
