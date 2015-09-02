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
package org.spongepowered.common.data.util;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.data.BlockValueProcessor;

/**
 * This is really just a lazy class to handle processing on multiple
 * {@link BlockValueProcessor} registrations.
 *
 * @param <E>
 * @param <V>
 */
public final class BlockValueProcessorDelegate<E, V extends BaseValue<E>> implements BlockValueProcessor<E, V> {

    private final Key<V> key;
    private final ImmutableList<BlockValueProcessor<E, V>> processors;

    public BlockValueProcessorDelegate(Key<V> key, ImmutableList<BlockValueProcessor<E, V>> processors) {
        this.key = key;
        this.processors = processors;
    }

    @Override
    public Key<? extends BaseValue<E>> getKey() {
        return this.key;
    }

    @Override
    public final int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean supports(IBlockState container) {
        for (BlockValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(container)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<E> getValueForBlockState(IBlockState blockState) {
        for (BlockValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(blockState)) {
                final Optional<E> optional = processor.getValueForBlockState(blockState);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<V> getApiValueForBlockState(IBlockState blockState) {
        for (BlockValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(blockState)) {
                final Optional<V> optional = processor.getApiValueForBlockState(blockState);
                if (optional.isPresent()) {
                    return optional;
                }
            }
        }
        return Optional.absent();
    }

    @Override
    public Optional<BlockState> offerValue(IBlockState blockState, E baseValue) {
        for (BlockValueProcessor<E, V> processor : this.processors) {
            if (processor.supports(blockState)) {
                final Optional<BlockState> result = processor.offerValue(blockState, baseValue);
                if (result.isPresent()) {
                    return result;
                }
            }
        }
        return Optional.absent();
    }
}
