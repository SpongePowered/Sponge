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
package org.spongepowered.common.data.manipulator.immutable.common;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.ImmutableListData;
import org.spongepowered.api.data.manipulator.mutable.ListData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableListValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeListValue;
import org.spongepowered.common.util.ReflectionUtil;

import java.lang.reflect.Modifier;
import java.util.List;

public abstract class AbstractImmutableListData<E, I extends ImmutableListData<E, I, M>, M extends ListData<E, M, I>>
        extends AbstractImmutableSingleData<List<E>, I, M> implements ImmutableListData<E, I, M> {

    private final Class<? extends M> mutable;
    private final ImmutableListValue<E> listValue;

    protected AbstractImmutableListData(Class<I> manipulatorClass, List<E> value,
                                              Key<? extends BaseValue<List<E>>> usedKey,
                                              Class<? extends M> mutableClass) {
        super(manipulatorClass, ImmutableList.copyOf(value), usedKey);
        checkArgument(!Modifier.isAbstract(mutableClass.getModifiers()), "The immutable class cannot be abstract!");
        checkArgument(!Modifier.isInterface(mutableClass.getModifiers()), "The immutable class cannot be an interface!");
        this.mutable = mutableClass;
        this.listValue = new ImmutableSpongeListValue<>(this.usedKey, ImmutableList.copyOf(this.value));
    }

    @Override
    protected final ImmutableListValue<E> getValueGetter() {
        return this.listValue;
    }

    @Override
    public List<E> getValue() {
        return ImmutableList.copyOf(super.getValue());
    }

    @Override
    public M asMutable() {
        return ReflectionUtil.createInstance(this.mutable, this.value);
    }

    @Override
    public ImmutableListValue<E> getListValue() {
        return getValueGetter();
    }

    @Override
    public List<E> asList() {
        return getValue();
    }
}
