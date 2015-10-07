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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.data.value.mutable.SpongeListValue;
import org.spongepowered.common.util.ReflectionUtil;

import java.lang.reflect.Modifier;
import java.util.List;

public abstract class AbstractImmutableSingleListData<E, I extends ImmutableDataManipulator<I, M>, M extends DataManipulator<M, I>>
    extends AbstractImmutableSingleData<List<E>, I, M> {

    private final Class<? extends M> mutable;

    public AbstractImmutableSingleListData(Class<I> manipulatorClass, List<E> value,
                                           Key<? extends BaseValue<List<E>>> usedKey,
                                           Class<? extends M> mutableClass) {
        super(manipulatorClass, ImmutableList.copyOf(value), usedKey);
        checkArgument(!Modifier.isAbstract(mutableClass.getModifiers()), "The immutable class cannot be abstract!");
        checkArgument(!Modifier.isInterface(mutableClass.getModifiers()), "The immutable class cannot be an interface!");
        this.mutable = mutableClass;
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return new ImmutableSpongeListValue<>(this.usedKey, ImmutableList.copyOf(this.value));
    }

    @Override
    public M asMutable() {
        return ReflectionUtil.createInstance(this.mutable, this.value);
    }

    @Override
    public int compareTo(I o) {
        return 0;
    }
}
