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
package org.spongepowered.common.command.parameter;

import org.spongepowered.api.command.parameter.Parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpongeParameterSequenceBuilder implements Parameter.SequenceBuilder {

    private final List<Parameter> parameters = new ArrayList<>();
    private boolean isOptional = false;
    private boolean isOptionalWeak = false;

    @Override
    public Parameter.SequenceBuilder optional() {
        this.isOptional = true;
        this.isOptionalWeak = false;
        return this;
    }

    @Override
    public Parameter.SequenceBuilder optionalWeak() {
        this.isOptional = false;
        this.isOptionalWeak = true;
        return this;
    }

    @Override
    public Parameter.SequenceBuilder then(Parameter parameter) {
        this.parameters.add(parameter);
        return this;
    }

    @Override
    public Parameter build() {
        return new SpongeSequenceParameter(this.parameters, this.isOptional || this.isOptionalWeak, this.isOptionalWeak);
    }

    @Override
    public Parameter.SequenceBuilder from(Parameter value) {
        // TODO: From
        throw new UnsupportedOperationException();
    }

    @Override
    public Parameter.SequenceBuilder reset() {
        this.isOptional = false;
        this.isOptionalWeak = false;
        this.parameters.clear();
        return this;
    }

}
