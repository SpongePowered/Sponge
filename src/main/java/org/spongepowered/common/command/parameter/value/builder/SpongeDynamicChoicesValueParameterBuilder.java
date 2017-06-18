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
import org.spongepowered.common.command.parameter.value.DynamicChoicesValueParameter;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class SpongeDynamicChoicesValueParameterBuilder implements VariableValueParameters.DynamicChoicesBuilder {

    @Nullable private Supplier<Iterable<String>> choiceSupplier = null;
    @Nullable private Function<String, ?> resultFunction = null;

    private boolean showInUsage = true;

    @Override
    public VariableValueParameters.DynamicChoicesBuilder setChoicesAndResults(Supplier<Map<String, ?>> choices) {
        return setChoices(() -> choices.get().keySet()).setResults(x -> choices.get().get(x));
    }

    @Override
    public VariableValueParameters.DynamicChoicesBuilder setChoices(Supplier<Iterable<String>> choices) {
        this.choiceSupplier = choices;
        return this;
    }

    @Override
    public VariableValueParameters.DynamicChoicesBuilder setResults(Function<String, ?> results) {
        this.resultFunction = results;
        return this;
    }

    @Override
    public VariableValueParameters.DynamicChoicesBuilder setShowInUsage(boolean showInUsage) {
        this.showInUsage = showInUsage;
        return this;
    }

    @Override
    public VariableValueParameters.DynamicChoicesBuilder from(ValueParameter value) {
        Preconditions.checkArgument(value instanceof DynamicChoicesValueParameter, "Must be a DynamicChoicesValueParameter");
        DynamicChoicesValueParameter dcvp = (DynamicChoicesValueParameter) value;
        this.showInUsage = dcvp.getIncludeChoicesInUsage() != Tristate.FALSE;
        this.resultFunction = dcvp.getResultFunction();
        this.choiceSupplier = dcvp.getChoiceSupplier();
        return this;
    }

    @Override
    public VariableValueParameters.DynamicChoicesBuilder reset() {
        this.choiceSupplier = null;
        this.resultFunction = null;
        this.showInUsage = true;
        return this;
    }

    @Override
    public ValueParameter build() {
        Preconditions.checkState(this.choiceSupplier != null, "choiceSupplier");
        Preconditions.checkState(this.resultFunction != null, "resultFunction");
        return new DynamicChoicesValueParameter(this.choiceSupplier, this.resultFunction, this.showInUsage ? Tristate.UNDEFINED : Tristate.FALSE);
    }

}
