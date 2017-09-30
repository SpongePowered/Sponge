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
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.util.ReflectionUtil;

import java.lang.reflect.Modifier;

public abstract class AbstractImmutableBooleanData<I extends ImmutableDataManipulator<I, M>, M extends DataManipulator<M, I>> extends
        AbstractImmutableSingleData<Boolean, I, M> {

    private final Class<? extends M> mutableClass;
    private final boolean defaultValue;
    private final ImmutableValue<Boolean> immutableValue;

    public AbstractImmutableBooleanData(Class<I> immutableClass, boolean value, Key<? extends BaseValue<Boolean>> usedKey,
                                        Class<? extends M> mutableClass, boolean defaultValue) {
        super(immutableClass, value, usedKey);
        checkArgument(!Modifier.isAbstract(mutableClass.getModifiers()), "The immutable class cannot be abstract!");
        checkArgument(!Modifier.isInterface(mutableClass.getModifiers()), "The immutable class cannot be an interface!");
        this.mutableClass = checkNotNull(mutableClass);
        this.defaultValue = defaultValue;
        this.immutableValue = ImmutableSpongeValue.cachedOf(usedKey, defaultValue, value);
    }

    @Override
    protected final ImmutableValue<Boolean> getValueGetter() {
        return this.immutableValue;
    }

    @Override
    public M asMutable() {
        return ReflectionUtil.createInstance(this.mutableClass, this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return super.toContainer()
            .set(this.usedKey, this.value);
    }

}
