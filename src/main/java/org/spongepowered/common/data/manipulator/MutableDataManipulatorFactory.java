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
package org.spongepowered.common.data.manipulator;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataManipulator;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.copy.CopyHelper;

import java.util.HashMap;
import java.util.Map;

public final class MutableDataManipulatorFactory implements DataManipulator.Mutable.Factory {

    @Override
    public DataManipulator.Mutable of() {
        return new MutableDataManipulator();
    }

    @Override
    public DataManipulator.Mutable of(Iterable<? extends Value<?>> values) {
        return new MutableDataManipulator(mapValues(values));
    }

    static Map<Key<?>, Object> mapValues(Iterable<? extends Value<?>> values) {
        checkNotNull(values, "values");
        final Map<Key<?>, Object> mappedValues = new HashMap<>();
        for (final Value<?> value : values) {
            mappedValues.put(value.getKey(), CopyHelper.copy(value.get()));
        }
        return mappedValues;
    }

    @Override
    public DataManipulator.Mutable of(ValueContainer valueContainer) {
        checkNotNull(valueContainer, "valueContainer");
        final MutableDataManipulator manipulator = new MutableDataManipulator();
        manipulator.copyFrom(valueContainer);
        return manipulator;
    }
}
