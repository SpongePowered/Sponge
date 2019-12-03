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
package org.spongepowered.common.data.processor.common;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataManipulator.Immutable;
import org.spongepowered.api.data.DataManipulator.Mutable;
import org.spongepowered.api.data.value.MergeFunction;
import java.util.Optional;

public abstract class AbstractMultiDataProcessor<T extends Mutable<T, I>, I extends Immutable<I, T>> extends AbstractSpongeDataProcessor<T, I> {

    protected abstract T createManipulator();

    @Override
    public Optional<T> createFrom(DataHolder dataHolder) {
        if (!this.supports(dataHolder)) {
            return Optional.empty();
        }
        Optional<T> optional = this.from(dataHolder);
        if (!optional.isPresent()) {
            return Optional.of(this.createManipulator());
        }
        return optional;
    }

    @Override
    public Optional<T> fill(DataHolder dataHolder, T manipulator, MergeFunction overlap) {
        if (!this.supports(dataHolder)) {
            return Optional.empty();
        }
        final T merged = checkNotNull(overlap).merge(manipulator.copy(), this.from(dataHolder).orElse(null));
        merged.getValues().forEach(manipulator::set);
        return Optional.of(manipulator);
    }

}
