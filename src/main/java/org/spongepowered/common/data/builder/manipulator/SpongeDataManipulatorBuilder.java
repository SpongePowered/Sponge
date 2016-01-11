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
package org.spongepowered.common.data.builder.manipulator;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.common.data.util.DataFunction;
import org.spongepowered.common.data.util.DataProcessorDelegate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Optional;

public final class SpongeDataManipulatorBuilder<T extends DataManipulator<T, I>, I extends ImmutableDataManipulator<I, T>>
    implements DataManipulatorBuilder<T, I> {

    private final DataProcessorDelegate<T, I> delegate;
    private final Class<T> manipulatorClass;
    private final Constructor<T> constructor;
    private final DataFunction<DataContainer, T, Optional<T>> buildFunction;

    public SpongeDataManipulatorBuilder(DataProcessorDelegate<T, I> delegate, Class<T> manipulatorClass, DataFunction<DataContainer, T, Optional<T>> buildFunction) {
        this.delegate = checkNotNull(delegate);
        checkNotNull(manipulatorClass);
        checkArgument(!Modifier.isAbstract(manipulatorClass.getModifiers()));
        checkArgument(!Modifier.isInterface(manipulatorClass.getModifiers()));
        this.manipulatorClass = manipulatorClass;
        try {
            this.constructor = manipulatorClass.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No no-args constructor found for class: " + manipulatorClass.getCanonicalName(), e);
        }
        this.buildFunction = checkNotNull(buildFunction);
    }

    @Override
    public Optional<T> createFrom(DataHolder dataHolder) {
        return this.delegate.createFrom(dataHolder);
    }

    @Override
    public T create() {
        try {
            return this.constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("No no-args constructor found for class: " + this.manipulatorClass.getCanonicalName(), e);
        }
    }

    @Override
    public Optional<T> build(DataView container) throws InvalidDataException {
        final DataContainer usedContainer;
        if (container instanceof DataContainer) {
            usedContainer = (DataContainer) container;
        } else {
            usedContainer = container.copy();
        }
        return this.buildFunction.apply(usedContainer, create());
    }
}
