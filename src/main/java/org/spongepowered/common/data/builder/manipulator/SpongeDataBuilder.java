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

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.SpongeDataRegistry;

import java.util.Optional;

public class SpongeDataBuilder<M extends DataManipulator<M, I>, I extends ImmutableDataManipulator<I, M>> implements DataManipulatorBuilder<M, I> {

    private final Class<? extends M> implClass;
    final I immutableSingleInstance;
    private DataProcessor<M, I> processor;

    public SpongeDataBuilder(Class<? extends M> implClass, I immutableSingleInstance, DataProcessor<M, I> processor) {
        this.implClass = implClass;
        this.immutableSingleInstance = immutableSingleInstance;
        this.processor = processor;
    }

    @Override
    public M create() {
        return this.immutableSingleInstance.asMutable();
    }

    @Override
    public Optional<M> createFrom(DataHolder dataHolder) {
        return this.processor.createFrom(dataHolder);
    }

    @Override
    public Optional<M> build(DataView view) throws InvalidDataException {
        DataContainer container;
        if (view instanceof DataContainer) {
            container = (DataContainer) view;
        } else {
            container = view.copy();
        }
        return this.processor.fill(container, create());
    }
    
    @SuppressWarnings("unchecked")
    public void finalizeRegistration() {
        this.processor = SpongeDataRegistry.getInstance().getProcessor((Class<M>) implClass).get();
    }
}
