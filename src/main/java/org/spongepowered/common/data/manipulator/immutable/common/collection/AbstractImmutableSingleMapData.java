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
package org.spongepowered.common.data.manipulator.immutable.common.collection;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableMapValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeMapValue;
import org.spongepowered.common.util.ReflectionUtil;

import java.lang.reflect.Modifier;
import java.util.Map;

public abstract class AbstractImmutableSingleMapData<K, V, I extends ImmutableDataManipulator<I, M>, M extends DataManipulator<M, I>>
    extends AbstractImmutableSingleData<Map<K, V>, I, M> {

    private final Class<? extends M> mutableClass;
    private final ImmutableMapValue<K, V> mapValue;

    public AbstractImmutableSingleMapData(Class<I> immutableClass, Map<K, V> value,
                                          Key<? extends BaseValue<Map<K, V>>> usedKey,
                                          Class<? extends M> manipulatorClass) {
        super(immutableClass, ImmutableMap.copyOf(value), usedKey);
        checkArgument(!Modifier.isAbstract(manipulatorClass.getModifiers()), "The immutable class cannot be abstract!");
        checkArgument(!Modifier.isInterface(manipulatorClass.getModifiers()), "The immutable class cannot be an interface!");
        this.mutableClass = manipulatorClass;
        this.mapValue = new ImmutableSpongeMapValue<>(this.usedKey, ImmutableMap.copyOf(this.value));
    }

    @Override
    protected final ImmutableMapValue<K, V> getValueGetter() {
        return this.mapValue;
    }

    @Override
    public M asMutable() {
        return ReflectionUtil.createInstance(this.mutableClass, this.value);
    }

    @Override
    public int compareTo(I o) {
        return 0;
    }
}
