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
package org.spongepowered.common.data.processor.value.entity;

import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.core.entity.item.EntityFallingBlockAccessor;
import org.spongepowered.common.util.Constants;

import java.util.Optional;

public class FallingBlockStateValueProcessor extends AbstractSpongeValueProcessor<EntityFallingBlockAccessor, BlockState, Value<BlockState>> {

    public FallingBlockStateValueProcessor() {
        super(EntityFallingBlockAccessor.class, Keys.FALLING_BLOCK_STATE);
    }

    @Override
    protected Value<BlockState> constructValue(final BlockState value) {
        return new SpongeValue<>(Keys.FALLING_BLOCK_STATE, Constants.Catalog.DEFAULT_FALLING_BLOCK_BLOCKSTATE, value);
    }

    @Override
    protected boolean set(final EntityFallingBlockAccessor container, final BlockState value) {
        container.accessor$setFallBlockState((net.minecraft.block.BlockState) value);
        return true;
    }

    @Override
    protected Optional<BlockState> getVal(final EntityFallingBlockAccessor container) {
        return Optional.of((BlockState)container.accessor$getFallBlockState());
    }

    @Override
    protected ImmutableValue<BlockState> constructImmutableValue(final BlockState value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
