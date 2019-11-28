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
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableCooldownData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.CooldownData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeCooldownData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;
import org.spongepowered.common.mixin.core.tileentity.TileEntityHopperAccessor;

import java.util.Optional;
import net.minecraft.tileentity.HopperTileEntity;

public class CooldownDataProcessor
        extends AbstractTileEntitySingleDataProcessor<HopperTileEntity, Integer, MutableBoundedValue<Integer>, CooldownData, ImmutableCooldownData> {

    public CooldownDataProcessor() {
        super(HopperTileEntity.class, Keys.COOLDOWN);
    }

    @Override
    public boolean set(final HopperTileEntity entity, final Integer value) {
        if (value < 1) {
            return false;
        }
        ((TileEntityHopperAccessor) entity ).accessor$setTransferCooldown(value);
        return true;
    }

    @Override
    public Optional<Integer> getVal(final HopperTileEntity entity) {
        return Optional.ofNullable(((TileEntityHopperAccessor) entity ).accessor$getTransferCooldown() < 1 ? null : ((TileEntityHopperAccessor) entity ).accessor$getTransferCooldown());
    }

    @Override
    protected MutableBoundedValue<Integer> constructValue(final Integer value) {
        return SpongeValueFactory.boundedBuilder(Keys.COOLDOWN)
                .minimum(1)
                .maximum(Integer.MAX_VALUE)
                .defaultValue(8)
                .actualValue(value)
                .build();
    }

    @Override
    public ImmutableValue<Integer> constructImmutableValue(final Integer value) {
        return constructValue(value).asImmutable();
    }

    @Override
    public CooldownData createManipulator() {
        return new SpongeCooldownData();
    }

    @Override
    public DataTransactionResult removeFrom(final ValueContainer<?> container) {
        if (container instanceof HopperTileEntity) {
            final HopperTileEntity hopper = (HopperTileEntity) container;
            final Optional<Integer> old = getVal(hopper);
            if (!old.isPresent()) {
                return DataTransactionResult.successNoData();
            }
            ((TileEntityHopperAccessor) hopper).accessor$setTransferCooldown(-1);
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        }
        return DataTransactionResult.failNoData();
    }

}
