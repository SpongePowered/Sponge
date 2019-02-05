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

import net.minecraft.tileentity.TileEntityHopper;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableCooldownData;
import org.spongepowered.api.data.manipulator.mutable.CooldownData;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeCooldownData;
import org.spongepowered.common.data.processor.common.AbstractTileEntitySingleDataProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.Optional;

public class CooldownDataProcessor
        extends AbstractTileEntitySingleDataProcessor<TileEntityHopper, Integer, CooldownData, ImmutableCooldownData> {

    public CooldownDataProcessor() {
        super(TileEntityHopper.class, Keys.COOLDOWN);
    }

    @Override
    public boolean set(TileEntityHopper entity, Integer value) {
        if (value < 1) {
            return false;
        }
        entity.transferCooldown = value;
        return true;
    }

    @Override
    public Optional<Integer> getVal(TileEntityHopper entity) {
        return Optional.ofNullable(entity.transferCooldown < 1 ? null : entity.transferCooldown);
    }

    @Override
    protected Value.Mutable<Integer> constructMutableValue(Integer value) {
        return SpongeValueFactory.boundedBuilder(Keys.COOLDOWN)
                .minimum(1)
                .maximum(Integer.MAX_VALUE)
                .value(value)
                .build();
    }

    @Override
    public Value.Immutable<Integer> constructImmutableValue(Integer value) {
        return constructMutableValue(value).asImmutable();
    }

    @Override
    public CooldownData createManipulator() {
        return new SpongeCooldownData();
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        if (container instanceof TileEntityHopper) {
            TileEntityHopper hopper = (TileEntityHopper) container;
            Optional<Integer> old = getVal(hopper);
            if (!old.isPresent()) {
                return DataTransactionResult.successNoData();
            }
            hopper.transferCooldown = -1;
            return DataTransactionResult.successRemove(constructImmutableValue(old.get()));
        }
        return DataTransactionResult.failNoData();
    }

}
