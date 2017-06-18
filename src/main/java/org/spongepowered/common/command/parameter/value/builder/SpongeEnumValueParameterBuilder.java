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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

public class SpongeEnumValueParameterBuilder implements VariableValueParameters.EnumBuilder {

    @Nullable private Class<? extends Enum<?>> enumClass = null;

    @Override
    public <T extends Enum<T>> VariableValueParameters.EnumBuilder setEnumClass(Class<T> enumClass) {
        this.enumClass = enumClass;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValueParameter build() {
        Preconditions.checkState(this.enumClass != null, "enumClass");

        // Create the choices
        Map<String, Supplier<?>> map = new HashMap<>();
        EnumSet<?> es = EnumSet.allOf((Class<? extends Enum>) this.enumClass);
        for (Enum<?> e : es) {
            map.put(e.name().toLowerCase(Locale.ENGLISH), () -> e);
        }

        return new ChoicesValueParameter(map, Tristate.TRUE);
    }

    @Override
    public VariableValueParameters.EnumBuilder from(ValueParameter value) {
        throw new UnsupportedOperationException("Not supported for this builder, use setEnumClass");
    }

    @Override
    public VariableValueParameters.EnumBuilder reset() {
        this.enumClass = null;
        return this;
    }

}
