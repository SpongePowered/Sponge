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
package org.spongepowered.common.data.processor.data.tileentity;

import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableDirectionalData;
import org.spongepowered.api.data.manipulator.mutable.block.DirectionalData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.mutable.block.SpongeDirectionalData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.mixin.core.tileentity.TileEntitySkullAccessor;

import java.util.Optional;
import net.minecraft.block.SkullBlock;
import net.minecraft.tileentity.SkullTileEntity;

public class SkullRotationDataProcessor
        extends AbstractTileEntitySingleDataProcessor<SkullTileEntity, Direction, Value<Direction>, DirectionalData, ImmutableDirectionalData> {

    public SkullRotationDataProcessor() {
        super(SkullTileEntity.class, Keys.DIRECTION);
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        // If a skull has rotation, you can't remove it
        return DataTransactionResult.failNoData();
    }

    @Override
    protected Value<Direction> constructValue(final Direction actualValue) {
        return new SpongeValue<>(Keys.DIRECTION, Direction.NONE, actualValue);
    }

    @Override
    protected boolean set(final SkullTileEntity skull, final Direction value) {
        if (value.ordinal() > 15) {
            return false;
        }
        skull.func_145903_a(value.ordinal());
        return true;
    }

    @Override
    protected Optional<Direction> getVal(final SkullTileEntity skull) {
        if (skull.func_145831_w().func_180495_p(skull.func_174877_v()).func_177229_b(SkullBlock.field_176418_a) != net.minecraft.util.Direction.UP) {
            return Optional.empty();
        }
        final int rot = ((TileEntitySkullAccessor) skull).accessor$getSkullRotation() % 16;
        return Optional.of(Direction.values()[rot < 0 ? rot + 16 : rot]);
    }

    @Override
    protected ImmutableValue<Direction> constructImmutableValue(final Direction value) {
        return ImmutableDataCachingUtil.getValue(ImmutableSpongeValue.class, Keys.DIRECTION, Direction.NORTH, value);
    }

    @Override
    protected DirectionalData createManipulator() {
        return new SpongeDirectionalData();
    }

}
