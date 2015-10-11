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

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.ImmutableDataHolder;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulatorBuilder;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.util.ReflectionUtil;

import java.util.Optional;

public class SpongeImmutableDataBuilder<I extends ImmutableDataManipulator<I, M>, M extends DataManipulator<M, I>> implements ImmutableDataManipulatorBuilder<I, M> {

    private final Class<? extends I> immutableManipulatorClass;
    private final SpongeDataBuilder<M, I> mutableBuilder;
    
    public SpongeImmutableDataBuilder(Class<? extends I> immutableManipulatorClass, SpongeDataBuilder<M, I> mutableBuilder) {
        this.immutableManipulatorClass = immutableManipulatorClass;
        this.mutableBuilder = mutableBuilder;
    }

    @Override
    public I createImmutable() {
        return ReflectionUtil.createInstance(this.immutableManipulatorClass);
    }

    @Override
    public Optional<I> createFrom(DataHolder dataHolder) {
        return this.mutableBuilder.createFrom(dataHolder).map(DataManipulator::asImmutable);
    }

    @Override
    public Optional<I> createFrom(ImmutableDataHolder<?> dataHolder) {
        return Optional.empty();
    }

    @Override
    public Optional<I> build(DataView view) throws InvalidDataException {
        return this.mutableBuilder.build(view).map(DataManipulator::asImmutable);
    }
}
